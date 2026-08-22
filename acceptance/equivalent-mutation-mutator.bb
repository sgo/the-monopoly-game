#!/usr/bin/env bb

(ns equivalent-mutation-mutator
  (:require [aps.cli.gherkin-mutator :as mutator]
            [aps.gherkin :as gherkin]
            [aps.mutation :as mutation]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn wrapper-args [args]
  (loop [remaining args
         project-root nil
         feature-id nil
         mutator-args []]
    (if-let [arg (first remaining)]
      (cond
        (= arg "--project-root")
        (recur (nnext remaining) (second remaining) feature-id mutator-args)

        (= arg "--feature-id")
        (recur (nnext remaining) project-root (second remaining) mutator-args)

        :else
        (recur (next remaining) project-root feature-id (conj mutator-args arg)))
      {:project-root project-root
       :feature-id feature-id
       :mutator-args mutator-args})))

(defn option-value [args option]
  (some (fn [[key value]]
          (when (= key option) value))
        (partition 2 1 args)))

(defn require-policy-fields [entry]
  (doseq [key [:feature :scenario :example :key :original :mutated :justification]]
    (when-not (contains? entry key)
      (throw (ex-info (str "equivalent mutation policy entry is missing " key) {}))))
  (when (str/blank? (:justification entry))
    (throw (ex-info "equivalent mutation policy entries require a justification" {})))
  entry)

(defn load-policy [root]
  (let [entries (edn/read-string (slurp (str (io/file root "acceptance/equivalent-mutations.edn"))))]
    (when-not (vector? entries)
      (throw (ex-info "equivalent mutation policy must be an EDN vector" {})))
    (mapv require-policy-fields entries)))

(defn policy-entry [entries feature-path feature mutation]
  (let [scenario (get-in feature [:scenarios (:scenario mutation)])]
    (some (fn [entry]
            (when (and (= feature-path (:feature entry))
                       (= (:name scenario) (:scenario entry))
                       (= (:example mutation) (:example entry))
                       (= (str (:key mutation)) (:key entry))
                       (= (:Original mutation) (:original entry))
                       (= (:Mutated mutation) (:mutated entry)))
              entry))
          entries)))

(defn- survivors-file [project-root feature-id]
  (let [slug (-> (str/lower-case feature-id)
                 (str/replace #"[^a-z0-9]+" "-")
                 (str/replace #"^-+|-+$" ""))]
    (str (io/file project-root "acceptance" (str "mutation-survivors-" slug ".md")))))

(defn- survivor-row [feature feature-id mutation]
  (let [scenario (get-in feature [:scenarios (:scenario mutation)])]
    (format "%s | %s | %s | %s | %s | %s"
            feature-id
            (:name scenario)
            (:example mutation)
            (:key mutation)
            (:Original mutation)
            (:Mutated mutation))))

(defn- by-id [mutations]
  (into {} (map (juxt :ID identity)) mutations))

(defn- write-survivors! [project-root feature-id feature mutations-by-id report]
  (let [survivors (->> (:results report)
                       (filter (fn [r] (= "survived" (:Status r))))
                       (map (fn [r] (survivor-row feature feature-id
                                                  (get mutations-by-id (get-in r [:Mutation :ID])))))
                       (into []))
        path (survivors-file project-root feature-id)]
    (if (seq survivors)
      (spit path (str "# Mutation survivors\n\n"
                      "Format: feature | scenario name | example index | key | original | mutated.\n\n"
                      (str/join "\n" survivors) "\n"))
      (io/delete-file path true))))

(defn- run-and-report [real-run project-root feature-id feature mutations-by-id cfg]
  (let [report (real-run cfg)]
    (write-survivors! project-root feature-id feature mutations-by-id report)
    report))

(defn- assert-tuple-field! [label value]
  (when (nil? value)
    (binding [*out* *err*] (println "FAIL survivor tuple field is null:" label))
    (System/exit 1)))

(defn- self-test []
  ;; Reproduce the defect contract: APS report results carry a stripped
  ;; :Mutation view (ID/Path/Description/Original/Mutated), while the full
  ;; discovery metadata (scenario/example/key) is joined back by mutation ID.
  (let [feature {:scenarios
                 [{:name "the report narrates an auction outcome"
                   :examples [{} {} {}]}
                  {:name "the report narrates a reserve alongside balance"
                   :examples [{}]}]}
        executable
        [{:ID "m1" :scenario 0 :example 1 :key "high_hat_bid"
          :Original "120" :Mutated "119"
          :Path "$.scenarios[0].examples[1].high_hat_bid"}
         {:ID "m2" :scenario 1 :example 0 :key "reserve"
          :Original "0" :Mutated "8"
          :Path "$.scenarios[1].examples[0].reserve"}
         {:ID "m3" :scenario 0 :example 2 :key "strategy"
          :Original "Billionaire" :Mutated "billionaire"
          :Path "$.scenarios[0].examples[2].strategy"}]
        mutations-by-id (by-id executable)
        report {:summary {:Total 3 :Survived 2 :Killed 1 :Errors 0}
                :results
                [{:Status "survived" :Mutation {:ID "m1"}}
                 {:Status "survived" :Mutation {:ID "m3"}}
                 {:Status "killed" :Mutation {:ID "m2"}}]}
        rows (->> (:results report)
                  (filter (fn [r] (= "survived" (:Status r))))
                  (map (fn [r] (survivor-row feature "en/report.feature"
                                             (get mutations-by-id (get-in r [:Mutation :ID])))))
                  (into []))]
    (when (not= 2 (count rows))
      (binding [*out* *err*] (println "FAIL expected 2 survivor rows, got" (count rows)))
      (System/exit 1))
    (doseq [row rows
            part (str/split (str/replace row #"^\S+ \| " "") #" \| ")]
      (assert-tuple-field! "row-part" part))
    (let [first-row (first rows)]
      (assert-tuple-field! "feature" first-row)
      (when-not (clojure.string/includes? first-row "the report narrates an auction outcome")
        (binding [*out* *err*] (println "FAIL scenario attribution wrong:" first-row))
        (System/exit 1))
      (when-not (clojure.string/includes? first-row " | 1 | high_hat_bid | 120 | 119")
        (binding [*out* *err*] (println "FAIL example/key/original/mutated attribution wrong:" first-row))
        (System/exit 1)))
    (println "self-test OK: 2 survivor rows, scenario/example/key/original/mutated all present")))
(let [{:keys [project-root feature-id mutator-args]} (wrapper-args *command-line-args*)
      feature-path (option-value mutator-args "--feature")]
  (when (= "--self-test" (first mutator-args))
    (self-test)
    (System/exit 0))
  (when (or (str/blank? project-root) (str/blank? feature-id) (str/blank? feature-path))
    (binding [*out* *err*]
      (println "usage: equivalent-mutation-mutator.bb --project-root <root> --feature <feature> [gherkin-mutator options...]"))
    (System/exit 2))
  (try
    (let [policy (load-policy project-root)
          feature (gherkin/parse-file feature-path)
          discovered (mutation/discover feature)
          matches (keep #(when-let [entry (policy-entry policy feature-id feature %)]
                           {:mutation % :entry entry}) discovered)
          skipped (set (map (comp :ID :mutation) matches))
          executable (vec (remove #(contains? skipped (:ID %)) discovered))]
      (binding [*out* *err*]
        (when (seq matches)
          (println "equivalent_skipped=" (count matches))
          (doseq [{:keys [mutation entry]} matches]
            (println "skipped equivalent" (:Description mutation) "--" (:justification entry)))))
      (with-redefs [mutation/discover (fn [_] executable)
                    mutation/run (partial run-and-report (var-get #'mutation/run) project-root feature-id feature
                                                        (by-id executable))]
        (apply mutator/-main mutator-args)))
    (catch Exception cause
      (binding [*out* *err*]
        (println (.getMessage cause)))
      (System/exit 1))))
