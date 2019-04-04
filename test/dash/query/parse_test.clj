(ns dash.query.parse-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [dash.query.parse :as parse]
            [dash.data.transform :as transform]
            [dash.persistence.io :as io]
            [test.util :refer :all]))

(deftest parse-node-to-string-test
  (is (= "a|b@c|d" (parse/parse-node-to-string "(a:b {c:d})")))
  (is (= "a|b|c" (parse/parse-node-to-string "(a:b:c)"))))

(deftest parse-edge-to-string-test
  (is (= "a|-|con|-|b" (parse/parse-edge-to-string "(a)-[con]-(b)"))))