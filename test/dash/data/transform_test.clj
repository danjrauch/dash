(ns dash.data.transform-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [test.util :refer :all]
            [dash.data.transform :as transform]))

(deftest bytes->num-test
  (is (= (transform/bytes->num (vec (map byte "AZEB"))) 1096435010)))

(deftest bytpes->string-test
  (is (= (transform/bytes->string (vec (map byte "AZEB"))) "AZEB")))