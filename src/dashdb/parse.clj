(ns dashdb.parse
  (:require [clojure.string :as str]
            [dashdb.query :as execute]))

(def keywords ["CREATE"])
(def node_re #"(\s*\(\s*([A-Za-z]{1,}:?){1,}(\s*,*\s*|\s*)(\s{1,}\{\s*[A-Za-z]{1,}:\s*([A-Za-z]{1,}|[0-9]{1,})((\s*,\s*|\s{1,})[A-Za-z]{1,}:\s*([A-Za-z]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")

(defn parse-create-block
  "Parse the CREATE block of the query and execute it.
   [string] -> nil"
  [create_block]
  (if (= (nth (re-find node_re create_block) 0) create_block)
    (do
      (def entities_without_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\)" %) (re-seq #"\(.*?\)" create_block))))
      (def entities_with_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\{.*?\}\s*\)" %) (re-seq #"\(.*?\)" create_block))))
      (loop [ls entities_with_props]
        (when (> (count ls) 0)
          (do
            (def subject_prop_split (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\s" 2))
            (def name_adjs (str/split (nth subject_prop_split 0) #"\:"))
            (def prop_pairs (str/split (subs (nth subject_prop_split 1) 1 (dec (count (nth subject_prop_split 1)))) #"\s*\:\s*"))
            )
          (recur (rest ls))
          )
        )
      (loop [ls entities_without_props]
        (when (> (count ls) 0)
          (def name_adjs (str/split (subs (nth ls 0) 1 (dec (count (nth ls 0)))) #"\:"))
          (recur (rest ls))
          )
        )
      )
    (println "Invalid create block. Please review and try again.")
    )
  )

(defn parse-input
  "Take the input from the cli and parse it into usable query.
   [] -> "
  [query_string]
  (if (and (> (count query_string) 4) (= (subs query_string 0 6) (nth keywords 0)))
    ; need to split query into blocks
    (parse-create-block (subs query_string 6))
    )
  )
