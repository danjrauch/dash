(ns dashdb.core
  (:require [dashdb.file :as f])
  (:gen-class))

(defn -main
  "Control HQ"
  [& args]
  (def file {:name "resources/t1" :type "inode"})
  (loop [x 100]
    (when (> x 1)
      (f/write-file (merge file {:content (rand-int 100)}))
      (f/write-file (merge file {:content "\n"}))
      (recur (- x 1))))
  (f/read-nth-block (merge file {:number 7}))
  )

(-main)
