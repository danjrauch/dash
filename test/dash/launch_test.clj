(ns dash.main-test
  (:require [clojure.test :refer :all]
            [dash.persistence.io :as io]
            [dash.core.main :as main]
            [test.util :refer :all]))

(deftest direct-query-input-test
  (with-files [["/node"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (main/direct-query-input "CREATE (a:b)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|b^"))))