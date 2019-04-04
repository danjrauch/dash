(ns dash.data.globals
  (:require [environ.core :as environ]))

(def global_graph_set (atom #{}))
(def global_graph_name (atom ""))
(def global_graph (atom {}))

(def data_dir
  (if (= (environ/env :clj-env) "test")
    "test/test_data/"
    "data/"))