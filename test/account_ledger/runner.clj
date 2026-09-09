(ns account-ledger.runner
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test]))

(defn- group-of [path]
  (cond (or (str/includes? path "/integration/")
            (str/includes? path "/ports/")
            (str/includes? path "/db/")) "integration"
        (str/includes? path "/e2e/") "e2e"
        :else "unit"))

(defn discover [groups]
  (->> (file-seq (io/file "test/account_ledger"))
       (filter #(.isFile %))
       (map #(.getPath %))
       (filter #(str/ends-with? % "_test.clj"))
       (filter #(contains? groups (group-of %)))
       sort
       (mapv #(-> % (str/replace #"^test/" "")
                  (str/replace #"\.clj$" "")
                  (str/replace "/" ".") (str/replace "_" "-") symbol))))

(defn -main [& groups]
  (try
    (let [selected (if (seq groups) (set groups) #{"unit" "integration" "e2e"})]
      (when-not (every? #{"unit" "integration" "e2e"} selected)
        (throw (ex-info "Unknown test group" {:groups selected})))
      (let [namespaces (discover selected)]
        (when (empty? namespaces) (throw (ex-info "No test namespaces discovered" {})))
        (doseq [n namespaces] (require n))
        (let [result (apply test/run-tests namespaces)
              ok? (and (pos? (:test result)) (zero? (+ (:fail result) (:error result))))]
          (println "Normal suite:" (pr-str result))
          (shutdown-agents)
          (System/exit (if ok? 0 1)))))
    (catch Throwable e
      (binding [*out* *err*] (println "Test runner error:" (.getMessage e)))
      (shutdown-agents)
      (System/exit 2))))
