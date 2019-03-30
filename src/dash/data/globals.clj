(ns dash.data.globals
  (:require [environ.core :as environ]
            [dash.persistence.io :as io]))

(def global_graph_set (atom #{}))
(def global_graph_name (atom ""))

(def data_dir 
  (if (= (environ/env :clj-env) "test")
    "test/test_data/"
    "data/"))