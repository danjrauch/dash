(ns dashdb.core
  (:require [clojure.java.io :as io])
  (:require [dashdb.file :as f])
  (:gen-class))

(defn -main
  "Control HQ"
  [& args]
  ; (println (type "hey"))
  (def file (f/createFile "resources/tbla" "a"))
  (f/transfer_content file 848484)
  )