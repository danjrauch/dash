(ns dashdb.crypto-test
  (:require [clojure.test :refer :all]
            [dashdb.crypto.id :refer :all :as id]
            [test.util :refer :all]))

(deftest crypto-create-id-test
  (is (= (count (id/create-id)) 10)))
