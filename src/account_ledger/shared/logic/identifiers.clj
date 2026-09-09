(ns account-ledger.shared.logic.identifiers
  "Pure identifier and logical-day predicates."
  (:require [clojure.string :as str]))

(defn id? [x] (and (string? x) (not (str/blank? x))))
(defn day? [x] (and (integer? x) (pos? x)))
(defn counter? [x] (and (integer? x) (not (neg? x))))
(defn system-id? [x] (and (id? x) (str/starts-with? x "system/")))
