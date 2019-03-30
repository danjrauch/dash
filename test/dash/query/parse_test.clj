(ns dash.query.parse-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [dash.query.parse :as parse]
            [dash.persistence.io :as io]
            [test.util :refer :all]))

(deftest parse-entity-with-properties-test
  (let [{name_adjs :name_adjs prop_pairs :prop_pairs} (parse/parse-entity-with-properties "(c:d {e:f})")]
    (is (= name_adjs '("c" "d")))
    (is (= prop_pairs '("e" "f")))))

(deftest parse-entity-without-properties-test
  (let [{name_adjs :name_adjs} (parse/parse-entity-without-properties "(a:b)")]
    (is (= name_adjs '("a" "b")))))

(deftest parse-create-node-block-test
  (let [{name_adjs :name_adjs} (parse/parse-create-node-block "(a:b)")]
    (is (= name_adjs '("a" "b"))))
  (let [{name_adjs :name_adjs prop_pairs :prop_pairs} (parse/parse-create-node-block "(c:d {e:f})")]
    (is (= name_adjs '("c" "d")))
    (is (= prop_pairs '("e" "f")))))

(deftest parse-create-relationship-block-test
  (let [{name_adjs :name_adjs prop_pairs :prop_pairs} (parse/parse-create-node-block "(c:d {e:f})")]
    (is (= name_adjs '("c" "d")))
    (is (= prop_pairs '("e" "f")))))