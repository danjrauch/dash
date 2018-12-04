(ns dashdb.core
  (:require [dashdb.file :as f])
  (:gen-class))

(defn -main
  "Control HQ"
  [& args]
  ; (println (type "hey"))
  (def file (f/createFile "resources/tbla" "a"))
  (loop [x 100]
    (when (> x 1)
      (f/transfer_content file (rand-int 100))
      (f/writeFile file "\n")
      (recur (- x 1))))
  (f/read_nth_block file 66)
  )

(-main)
