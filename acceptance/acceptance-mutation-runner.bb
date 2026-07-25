#!/usr/bin/env bb

;; Project runner adapter for `bb gherkin-mutator`.
;;
;; Persistent worker: reads one JSON job per line on stdin and writes one JSON
;; response per line on stdout. Diagnostics go to stderr, never stdout.
;;
;; This project's entrypoint generator embeds the IR in the generated Java
;; source rather than reading it at run time, so a mutated IR needs the entry
;; point regenerated before the tests can see it. Hiding that from the mutator
;; is exactly what the adapter is for.

(ns acceptance-mutation-runner
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def root (str (fs/parent (fs/parent (fs/absolutize *file*)))))
(def module "the-monopoly-game-specs/the-monopoly-game-specs-core")
(def generated (str (fs/path root module "target" "generated-test-sources" "acceptance")))
(def generator (str (fs/path root "acceptance" "acceptance-entrypoint-generator.bb")))

(defn- log [& parts]
  (binding [*out* *err*]
    (println (str/join " " parts))
    (flush)))

(defn- entry-point-class
  "The generator derives the class name from the IR file stem."
  [ir-path]
  (->> (-> (fs/file-name ir-path) (str/replace #"\.json$" "") (str/split #"[^A-Za-z0-9]+"))
       (remove str/blank?)
       (map str/capitalize)
       (str/join)
       (#(str % "AcceptanceTest"))))

(defn- regenerate! [feature-json]
  (fs/delete-tree generated)
  (let [{:keys [exit err]} (process/sh {:dir root} generator feature-json generated)]
    (when-not (zero? exit)
      (throw (ex-info (str "entrypoint generation failed: " err) {})))))

(defn- run-tests [class-name]
  (process/sh {:dir root}
              "mvn" "-B" "-o" "-pl" module
              (str "-Dtest=" class-name)
              "-Dsurefire.failIfNoSpecifiedTests=false"
              "test"))

(defn- outcome
  "A failing assertion means the mutated specification was detected. Anything
  that stops the tests from running at all is an infrastructure error, not a
  surviving mutation."
  [{:keys [exit out]}]
  (cond
    (zero? exit) "test_success"
    (re-find #"(?m)Tests run:.*Failures: [1-9]|Tests run:.*Errors: [1-9]|There are test failures" out) "test_failure"
    :else "infrastructure_error"))

(defn- handle [{:keys [id feature_json] :as job}]
  (let [started (System/nanoTime)]
    (try
      (regenerate! feature_json)
      (let [result (run-tests (entry-point-class feature_json))
            classified (outcome result)]
        {:id id
         :outcome classified
         :output (if (= "infrastructure_error" classified) (:out result) "")
         :error ""
         :duration (- (System/nanoTime) started)})
      (catch Exception cause
        (log "job" id "failed:" (ex-message cause))
        {:id id
         :outcome "infrastructure_error"
         :output ""
         :error (str (ex-message cause))
         :duration (- (System/nanoTime) started)}))))

(defn -main []
  (log "runner adapter ready")
  (doseq [line (line-seq (java.io.BufferedReader. *in*))
          :when (not (str/blank? line))]
    (let [response (handle (json/parse-string line true))]
      (println (json/generate-string response))
      (flush))))

(-main)
