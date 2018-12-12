(ns dashdb.core
  (:require [dashdb.fs :as fs]
            [dashdb.query :as q]
            [clojure.string :as str])
  (:gen-class))

(defn -main
  "Control HQ"
  [& args]
  (loop []
    (do (print "dash=> ") (flush)
      (let [input (read-line)]
        (if (not (= input "exit"))
          (do (q/parse-input (str/trim input)) (recur))
          )))))
