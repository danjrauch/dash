(ns dashdb.query
  (:require [clojure.string :as str]))

(def keywords ["CREATE"])
(def node_re #"(\s*\(\s*([A-Za-z]{1,}:?){1,}(\s*,*\s*|\s*)(\s{1,}\{\s*[A-Za-z]{1,}:\s*([A-Za-z]{1,}|[0-9]{1,})((\s*,\s*|\s{1,})[A-Za-z]{1,}:\s*([A-Za-z]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")

(defn parse-create-block
  [create_block]
  ; check if the block is valid
  (if (= (nth (re-find node_re create_block) 0) create_block)
    (do
      (def entities_without_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\)" %) (re-seq #"\(.*?\)" create_block))))
      (def entities_with_props (remove nil? (filter #(re-seq #"\(\s*[A-Za-z:]*?\s*\{.*?\}\s*\)" %) (re-seq #"\(.*?\)" create_block))))
      (loop [ls entities_without_props]
        (when (> (count ls) 0)
          (dorun (map print (str/split (subs (nth ls 0) 1 (- (count (nth ls 0)) 1)) #"\:")))
          (println)
          (recur (rest ls))
          )
        )
      (println "Done")
      )
    (println "Invalid create block. Please review and try again.")
    )
  )

(defn parse-input
  "Take the input from the cli and parse it into usable query."
  [query_string]
  (if (and (> (count query_string) 4) (= (subs query_string 0 6) (nth keywords 0)))
    ; need to split query into blocks
    (parse-create-block (subs query_string 6))
    )
  )
