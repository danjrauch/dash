(ns dashdb.query.parse
  (:require [clojure.string :as str]
            [dashdb.query.execute :as execute]
            [dashdb.query.regex :refer :all]
            [dashdb.persistence.io :refer :all :as io]
            [environ.core :as environ]
            [clj-time.core :as t]
            [clj-time.local :as l]))

;; When there are parenthesized groups in the pattern and re-find finds a match, it returns a vector.  The first item is the part of
;; the string that matches the entire pattern, and each successive item are the parts of the string that matched the 1st, 2nd,
;; etc. parenthesized groups.  Groups are numbered by the order in which their left parenthesis occurs in the string, from left to
;; right.

(defn parse-create-node-block
  "Parse the create node block of the query and execute it.
   [string] -> nil"
  [create_block]
  (def entities_without_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z0-9:]*?\s*\)" %) (re-seq #"\(.*?\)" create_block))))
  (def entities_with_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z0-9:]*?\s*\{.*?\}\s*\)" %) (re-seq #"\(.*?\)" create_block))))
  (def ids (atom '()))
  (loop [ls entities_with_props]
    (when (> (count ls) 0)
      (def subject_prop_split (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\s{1,}" 2))
      (def name_adjs (map str/trim (str/split (nth subject_prop_split 0) #"\:")))
      (def prop_string (subs (nth subject_prop_split 1) 1 (dec (count (nth subject_prop_split 1)))))
      (def prop_pairs (remove #(re-matches #"\s*" %) (map str/trim (str/split prop_string #"(\s*:\s*|\s*,\s*|\s{1,})"))))
      (swap! ids conj (execute/create-node name_adjs prop_pairs))
      (recur (rest ls))))
  (loop [ls entities_without_props]
    (when (> (count ls) 0)
      (def name_adjs (map str/trim (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\:")))
      (swap! ids conj (execute/create-node name_adjs))
      (recur (rest ls))))
  ;; Transaction writing
  (if (= (environ/env :clj-env) "test")
    (def tfilename "test/test_data/transactions")
    (def tfilename "data/transactions"))
  (io/pin-file BM tfilename)
  (def tfile (io/get-file BM tfilename))
  (io/append-content tfile (str (standard-datetime) "|" (str/join \: @ids) "\n"))
  (io/write-to-file tfile))

(defn parse-create-rel-block
  "Parse the create relationship block of the query and return it.
   [string] -> nil"
  [create_block]
  (def ids (atom '()))
  (loop [rs (re-seq #"\([A-Za-z0-9:]{1,}\)(-|<-)\[:[A-Za-z0-9:]{1,}\](-|->)\([A-Za-z0-9:]{1,}\)" create_block)]
    (when (> (count rs) 0)
      (swap! ids conj (execute/create-rel (map str/trim (subvec (str/split (nth (nth rs 0) 0) #"\(|\)|\[|\]") 1))))
      (recur (rest rs))))
  ;; Transaction writing
  (if (= (environ/env :clj-env) "test")
    (def tfilename "test/test_data/transactions")
    (def tfilename "data/transactions"))
  (io/pin-file BM tfilename)
  (def tfile (io/get-file BM tfilename))
  (io/append-content tfile (str (standard-datetime) "|" (str/join \: @ids) "\n"))
  (io/write-to-file tfile))

(defn parse-input
  "Take the input from the cli and parse it into usable query.
   [string] -> nil"
  [query_string]
  ; Match the query_string to one of the recognized forms
  (cond
    ; The query_string is a create-node clause
    (= query_string (nth (re-find create_node_re query_string) 0)) (parse-create-node-block (subs query_string 6))
    ; The query_stringis a create-rel clause
    (= query_string (nth (re-find create_rel_re query_string) 0)) (parse-create-rel-block (subs query_string 6))
    :else (println "Invalid query string. Please review and try again.")))
