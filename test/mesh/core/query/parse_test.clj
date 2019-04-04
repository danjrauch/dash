(ns mesh.core.query.parse-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [mesh.core.query.parse :as parse]
            [mesh.core.data.transform :as transform]
            [mesh.core.persistence.io :as io]
            [test.util :refer :all]))

(deftest parse-node-to-string-test
  (is (= "a|b@c|d" (parse/parse-node-to-string "(a:b {c:d})")))
  (is (= "a|b|c" (parse/parse-node-to-string "(a:b:c)"))))

(deftest parse-edge-to-string-test
  (is (= "a|-|con|-|b" (parse/parse-edge-to-string "(a)-[con]-(b)"))))