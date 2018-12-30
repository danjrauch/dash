(ns dashdb.query.parse
  (:require [clojure.string :as str]
            [dashdb.query.execute :as execute]
            [dashdb.query.regex :refer :all]))

(defn parse-create-block
  "Parse the CREATE block of the query and execute it.
   [string] -> nil"
  [create_block]
  (def entities_without_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\)" %) (re-seq #"\(.*?\)" create_block))))
  (def entities_with_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\{.*?\}\s*\)" %) (re-seq #"\(.*?\)" create_block))))
  (loop [ls entities_with_props]
    (when (> (count ls) 0)
      (def subject_prop_split (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\s{1,}" 2))
      (def name_adjs (str/split (nth subject_prop_split 0) #"\:"))
      (def prop_string (subs (nth subject_prop_split 1) 1 (dec (count (nth subject_prop_split 1)))))
      (def prop_pairs (remove #(re-matches #"\s*" %) (str/split prop_string #"(\s*:\s*|\s*,\s*|\s{1,})")))
      (execute/create name_adjs prop_pairs)
      (recur (rest ls))
      )
    )
  (loop [ls entities_without_props]
    (when (> (count ls) 0)
      (def name_adjs (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\:"))
      (execute/create name_adjs)
      (recur (rest ls))
      )
    )
  )

(defn parse-input
  "Take the input from the cli and parse it into usable query.
   [string] -> nil"
  [query_string]
  ; Match the query_string to one of the recognized forms
  (cond
    ; The query_string is a CREATE clause
    (= query_string (nth (re-find create_re query_string) 0)) (parse-create-block (subs query_string 6))
    ; (println query_string );(nth (re-find create_re query_string) 0))
    :else (println "Invalid query string. Please review and try again.")
    )
  )
