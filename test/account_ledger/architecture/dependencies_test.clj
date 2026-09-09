(ns account-ledger.architecture.dependencies-test
  "The requested pure domain boundary is an executable architecture constraint."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(defn- logic-sources []
  (->> (file-seq (io/file "src/account_ledger"))
       (filter #(.isFile %))
       (filter #(and (str/includes? (.getPath %) "/logic/")
                     (str/ends-with? (.getName %) ".clj")))))

(defn- read-source [file]
  (let [namespace (-> (.getPath file)
                      (str/replace #"^src/" "") (str/replace #"\.clj$" "")
                      (str/replace "/" ".") (str/replace "_" "-") symbol)]
    (require namespace)
    (binding [*ns* (the-ns namespace) *read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader file))]
        {:namespace namespace
         :forms (loop [forms []]
                  (let [form (read {:eof ::eof} reader)]
                    (if (= ::eof form) forms (recur (conj forms form)))))}))))

(defn- referenced-namespaces [namespace forms]
  (let [aliases (ns-aliases namespace)]
    (->> (tree-seq coll? seq forms)
         (filter symbol?)
         (keep #(or (clojure.core/namespace %)
                    (when (str/starts-with? (str %) "account-ledger.") (str %))))
         (map #(if-let [target (get aliases (symbol %))] (str (ns-name target)) %))
         set)))

(deftest pure-logic-cannot-depend-on-ports-storage-or-application-entry-points
  (let [sources (logic-sources)]
    (is (seq sources))
    (doseq [file sources
            :let [{:keys [namespace forms]} (read-source file)]]
      (testing (.getPath file)
        (let [invalid-dependencies (filter #(and (str/starts-with? % "account-ledger.")
                                                (not (re-find #"\.(logic|model)\." %)))
                                           (referenced-namespaces namespace forms))
              forbidden '#{atom agent ref future future-call promise deref swap! reset! compare-and-set!
                          alter ref-set send send-off deliver locking dosync
                          slurp spit println print prn printf read-line load-file eval}
              effects (set (for [symbol (filter symbol? (tree-seq coll? seq forms))
                                 :let [resolved (ns-resolve namespace symbol)]
                                 :when (and (var? resolved)
                                            (= 'clojure.core (ns-name (:ns (meta resolved))))
                                            (contains? forbidden (:name (meta resolved))))]
                             symbol))]
          (is (empty? invalid-dependencies) (str "Forbidden dependencies: " invalid-dependencies))
          (is (empty? effects) (str "Forbidden effects: " effects)))))))
