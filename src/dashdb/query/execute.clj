(ns dashdb.query.execute
  (:require [dashdb.persistence.io :as io]))

(defn create
  "Execute the CREATE action for an individual record.
   [vector & vector] -> nil"
  [name_adjs & prop_pairs]
  ; (if prop_pairs
  ;   (println "hey")
  ;   )
  (def file (io/read-from-file {:name "data/node"}))
  (println (:contents file))
  (def res (io/bytes->num (subvec (:contents file) 0 4)))
  (println res)
  )