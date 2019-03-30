(ns dash.data.read
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]
            [dash.data.globals :refer :all]))

(defn get-graph-names
  ""
  []
  (def graph_name_file (io/provide-file (str data_dir "graph_names")))
  (def graph_name_file_contents (transform/bytes->string (io/get-contents graph_name_file)))
  (def graph_name_list (str/split-lines graph_name_file_contents))
  (set graph_name_list))