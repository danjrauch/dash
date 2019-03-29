(ns dash.persistence.read
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]))

(defn get-graph-names
  ""
  []
  (def graph_name_file (io/provide-file "data/graph_names"))
  (println (io/get-contents graph_name_file))
  )