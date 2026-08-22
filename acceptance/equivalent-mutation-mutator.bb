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

(defn- write-survivors! [project-root feature-id feature report]
  (let [survivors (into []
                        (comp (filter (fn [r] (= "survived" (:Status r))))
                              (map (fn [r] (survivor-row feature feature-id (get-in r [:Mutation])))))
                        (:results report))
        path (survivors-file project-root feature-id)]
    (if (seq survivors)
      (spit path (str "# Mutation survivors\n\n"
                      "Format: feature | scenario name | example index | key | original | mutated.\n\n"
                      (str/join "\n" survivors) "\n"))
      (io/delete-file path true))))

(defn- run-and-report [real-run project-root feature-id feature cfg]
  (let [report (real-run cfg)]
    (write-survivors! project-root feature-id feature report)
    report))
(let [{:keys [project-root feature-id mutator-args]} (wrapper-args *command-line-args*)
      feature-path (option-value mutator-args "--feature")]
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
                    mutation/run (partial run-and-report (var-get #'mutation/run) project-root feature-id feature)]
        (apply mutator/-main mutator-args)))
    (catch Exception cause
      (binding [*out* *err*]
        (println (.getMessage cause)))
      (System/exit 1))))
