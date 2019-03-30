(ns dash.data.read-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.globals :refer :all]
            [dash.data.transform :as transform]
            [dash.data.read :as read]))

(deftest get-graph-names-test
  (with-files [["/graph_names" "X\nY\nZ\n"]]
    (def graph_name_set (read/get-graph-names))
    (is (true? (contains? graph_name_set "X")))
    (is (true? (contains? graph_name_set "Y")))
    (is (true? (contains? graph_name_set "Z")))))