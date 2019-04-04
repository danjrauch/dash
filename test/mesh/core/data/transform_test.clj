(ns mesh.core.data.transform-test
  (:require 
   [clojure.test :refer :all]
   [clojure.java.io :as javaio]
   [test.util :refer :all]
   [mesh.core.data.transform :as transform]))

(deftest bytes->num-test
  ; (fact (transform/bytes->num (vec (map byte "AZEB"))) => 1096435010)
  (is (= (transform/bytes->num (vec (map byte "AZEB"))) 1096435010)))

(deftest bytpes->string-test
  ; (fact (transform/bytes->string (vec (map byte "AZEB"))) => "AZEB")
  (is (= (transform/bytes->string (vec (map byte "AZEB"))) "AZEB")))

(deftest string->node-test
  (is (= (transform/string->node "a") {:name "a" :descriptor_set #{} :adjacency_map {}}))
  (is (= (transform/string->node "a|b") {:name "a" :descriptor_set #{"b"} :adjacency_map {}}))
  (is (= (transform/string->node "a|b|c") {:name "a" :descriptor_set #{"b" "c"} :adjacency_map {}}))
  (is (= (transform/string->node "a|b|c@c|d") {:name "a" :descriptor_set #{"b" "c"} :attribute_map {:c "d"} :adjacency_map {}}))
  (is (= (transform/string->node "a|b|c@c|d|e|f") {:name "a" :descriptor_set #{"b" "c"} :attribute_map {:c "d" :e "f"} :adjacency_map {}})))

(deftest string->edge-test
  (is (= (transform/string->edge "a|-|con|-|b") {:u "a" :direction "--" :v "b" :label "con" })))