(ns dash.query.parse
  (:require [clojure.string :as str]
            [dash.persistence.io :refer :all :as io]
            [environ.core :as environ]
            [clj-time.core :as t]
            [clj-time.local :as l]))

;; When there are parenthesized groups in the pattern and re-find finds a match, it returns a vector.  The first item is the part of
;; the string that matches the entire pattern, and each successive item are the parts of the string that matched the 1st, 2nd,
;; etc. parenthesized groups.  Groups are numbered by the order in which their left parenthesis occurs in the string, from left to
;; right.

(defn parse-entity-with-properties
  ""
  [block]
  (def subject_prop_split (str/split (subs block 1 (dec (count block))) #"\s{1,}" 2))
  (def name_adjs (map str/trim (str/split (nth subject_prop_split 0) #"\:")))
  (def prop_string (subs (nth subject_prop_split 1) 1 (dec (count (nth subject_prop_split 1)))))
  (def prop_pairs (remove #(re-matches #"\s*" %) (map str/trim (str/split prop_string #"(\s*:\s*|\s*,\s*|\s{1,})"))))
  {:name_adjs name_adjs :prop_pairs prop_pairs})

(defn parse-entity-without-properties
  ""
  [block]
  (def name_adjs (map str/trim (str/split (subs block 1 (dec (count block))) #"\:")))
  {:name_adjs name_adjs})

(defn parse-create-node-block
  "Parse the create node block of the query and execute it."
  [node_block]
  (if (not-empty (re-find #"\(\s*[A-Za-z0-9:]*?\s*\{.*?\}\s*\)" node_block))
    (parse-entity-with-properties node_block)
    (parse-entity-without-properties node_block)))

(defn parse-create-relationship-block
  "Parse the create relationship block of the query and return it."
  [relationship_block]
  (map str/trim (subvec (str/split relationship_block #"\(|\)|\[|\]") 1)))