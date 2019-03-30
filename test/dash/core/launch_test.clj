(ns dash.core.launch-test
  (:require [clojure.test :refer :all]
            [dash.persistence.io :as io]
            [dash.core.launch :as launch]
            [dash.data.transform :as transform]
            [test.util :refer :all]))

(deftest direct-query-input-test
  (with-files [["/graph_names" "X\n"] ["/X/node"] ["/X/rel"]]
    (def file (io/provide-file (str tmp_dir "/X/node")))
    (launch/direct-query-input "X" "CREATE (a:b)")
    (is (= (transform/bytes->string (io/get-contents file)) "a|b^"))
    (def file (io/provide-file (str tmp_dir "/X/rel")))
    (launch/direct-query-input "X" "CREATE (a)-[:b]->(c)")
    (is (= (transform/bytes->string (io/get-contents file)) "a|-|:b|->|c^"))))