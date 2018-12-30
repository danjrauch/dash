(ns dashdb.query.execute
  (:require [dashdb.persistence.io :as io]))

(defn create
  "Execute the CREATE action for an individual record.
   [vector & vector] -> nil"
  [name_adjs & prop_pairs]
  ; (if prop_pairs
  ;   (println "hey")
  ;   )
  (def file (io/create-file "data/node"))
  (io/read-from-file file)
  (io/concat-append file name_adjs)
  (io/write-to-file file)
  (println (type (io/get-contents file)))
  ; (def res (io/bytes->num (subvec (:contents file) 0 4)))
  )
