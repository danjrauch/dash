(ns mesh.persist-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.graph :as graph]
            [mesh.persist :as persist]))

;; Data Transform Tests

(deftest bytes->num-test
  (is (= (persist/bytes->num (vec (map byte "AZEB"))) 1096435010)))

(deftest bytpes->string-test
  (is (= (persist/bytes->string (vec (map byte "AZEB"))) "AZEB")))

;;; I/O Tests

(deftest write-to-disk-test
  (with-files [["t"]]
    (def file (persist/create-file (str tmp_dir "t")))
    (persist/read-from-disk file)
    (persist/append-string file "aaa")
    (persist/write-to-disk file)
    (persist/read-from-disk file)
    (is (= (persist/bytes->string (persist/get-contents file)) "aaa"))))

(deftest read-from-disk-test
  (with-files [["t" "12345"]]
    (def file (persist/create-file (str tmp_dir "t")))
    (persist/read-from-disk file)
    (is (= (persist/bytes->string (persist/get-contents file)) "12345"))))

(deftest create-file-on-write
  (with-files [["t" "1"]]
    (def file (persist/create-file (str tmp_dir "t")))
    (persist/append-string file "123")
    (persist/write-to-disk file)
    (is (= (persist/bytes->string (persist/get-contents file)) "123"))))

(deftest multiple-read-write-test
  (with-files [["t" "1"]]
    (def file (persist/create-file (str tmp_dir "t")))
    (persist/read-from-disk file)
    (is (= (persist/bytes->string (persist/get-contents file))) "1")
    (persist/append-string file "2")
    (persist/write-to-disk file)
    (persist/read-from-disk file)
    (is (= (persist/bytes->string (persist/get-contents file)) "12"))
    (persist/append-string file "345")
    (persist/write-to-disk file)
    (persist/read-from-disk file)
    (is (= (persist/bytes->string (persist/get-contents file)) "12345"))))

(deftest set-dirty-value-test
  (with-tmp-dir
    (def file (persist/create-file (str tmp_dir "t")))
    (persist/set-dirty-value file 1)
    (is (= (persist/get-dirty-value file) 1))
    (persist/set-dirty-value file 0)
    (is (= (persist/get-dirty-value file) 0))))

;;; Buffer Manager Tests

(deftest get-file-position-negative-test
  (def bm (persist/create-buffer-manager 5))
  (persist/get-file-position bm (str tmp_dir "t1"))
  (is (= (persist/get-file-position bm (str tmp_dir "t1")) nil)))

(deftest get-file-negative-test
  (def bm (persist/create-buffer-manager 5))
  (persist/get-file bm (str tmp_dir "t1"))
  (is (= (persist/get-file bm (str tmp_dir "t1")) nil)))

(deftest get-file-position-positive-test
  (def bm (persist/create-buffer-manager 5))
  (persist/get-file-position bm "No File")
  (is (= (persist/get-file-position bm "No File") 0)))

(deftest get-file-positive-test
  (def bm (persist/create-buffer-manager 5))
  (persist/get-file-position bm "No File")
  (is (= (persist/get-name (persist/get-file bm "No File")) "No File")))

(deftest pin-file-test
  (with-files [["t1"] ["t2"] ["t3"] ["t4"] ["t5"]]
    (def bm (persist/create-buffer-manager 5))
    (persist/pin-file bm (str tmp_dir "t1"))
    (is (= (persist/get-file-position bm (str tmp_dir "t1")) 0))
    (is (= (persist/get-name (persist/get-file bm (str tmp_dir "t1"))) (str tmp_dir "t1")))))

(deftest unpin-file-test
  (with-files [["t1"]]
    (def bm (persist/create-buffer-manager 10))
    (persist/pin-file bm (str tmp_dir "t1"))
    (def file (persist/get-file bm (str tmp_dir "t1")))
    (persist/append-string file "1234")
    (is (= (persist/bytes->string (persist/get-contents file)) "1234"))
    (is (= (persist/get-dirty-value file) true))
    (is (= (persist/get-r-value file) 1))
    (persist/unpin-file bm (str tmp_dir "t1"))
    (is (= (persist/get-file bm (str tmp_dir "t1")) nil))))

(deftest pin-file-to-overflow-test
  (with-files [["t1"] ["t2"] ["t3"] ["t4"] ["t5"] ["t6"] ["t7"] ["t8"] ["t9"] ["t10"]]
    (def bm (persist/create-buffer-manager 5))
    (doseq [x (range 1 11)]
      (persist/pin-file bm (str tmp_dir "t" x)))
    (is (= (persist/get-file-position bm (str tmp_dir "t6")) 0))
    (is (= (persist/get-file-position bm (str tmp_dir "t7")) 1))
    (is (= (persist/get-file-position bm (str tmp_dir "t8")) 2))
    (is (= (persist/get-file-position bm (str tmp_dir "t9")) 3))
    (is (= (persist/get-file-position bm (str tmp_dir "t10")) 4))))

(deftest pin-file-to-overflow-with-hotfile-test
  (with-files [["t1"] ["t2"] ["t3"] ["t4"] ["t5"] ["t6"] ["t7"] ["t8"] ["t9"] ["t10"]]
    (def bm (persist/create-buffer-manager 5))
    (doseq [x (range 1 11)]
      (persist/pin-file bm (str tmp_dir "t" x))
      (persist/pin-file bm (str tmp_dir "t1")))
    (is (= (persist/get-file-position bm (str tmp_dir "t10")) 0))
    (is (= (persist/get-file-position bm (str tmp_dir "t1")) 1))
    (is (= (persist/get-file-position bm (str tmp_dir "t7")) 2))
    (is (= (persist/get-file-position bm (str tmp_dir "t8")) 3))
    (is (= (persist/get-file-position bm (str tmp_dir "t9")) 4))))

(deftest pin-file-to-overflow-with-dirty-test
  (with-files [["t1" "12"] ["t2"] ["t3"] ["t4"] ["t5"] ["t6"] ["t7"] ["t8"] ["t9"] ["t10"]]
    (def bm (persist/create-buffer-manager 5))
    (persist/pin-file bm (str tmp_dir "t1"))
    (persist/append-string (persist/get-file bm (str tmp_dir "t1")) "34")
    (doseq [x (range 2 11)]
      (persist/pin-file bm (str tmp_dir "t" x)))
    (def file (persist/create-file (str tmp_dir "t1")))
    (persist/read-from-disk file)
    (is (= (persist/get-dirty-value file) false))
    (is (= (persist/bytes->string (persist/get-contents file)) "1234"))))

(deftest write-files-to-disk-test
  (with-files [["t1"] ["t2"] ["t3"] ["t4"] ["t5"] ["t6"] ["t7"] ["t8"] ["t9"] ["t10"]]
    (def bm (persist/create-buffer-manager 10))
    (doseq [x (range 1 11)]
      (persist/pin-file bm (str tmp_dir "t" x))
      (persist/append-string (persist/get-file bm (str tmp_dir "t" x)) (str x x x)))
    (persist/write-files-to-disk bm)
    (doseq [x (range 1 11)]
      (def file (persist/get-file bm (str tmp_dir "t" x)))
      (persist/read-from-disk file)
      (is (= (persist/bytes->string (persist/get-contents file)) (str x x x)))
      (is (= (persist/get-dirty-value file) false)))))

;;; Read/Write Tests

(deftest create-graph-test
  (with-files [["graph_names"]]
    (persist/create-graph "create_graph_test_graph" tmp_dir)
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "create_graph_test_graph/graph.fress")))))))

; (deftest write-node-test
  ; (with-tmp-dir
    ; (persist/create-graph "write_node_test_graph" tmp_dir)
    ; (persist/write-node "write_node_test_graph" {:name "a" :descriptor_set #{"b"} :attribute_map {} :adjacency_map {}} tmp_dir)
    ; (def node_file (persist/provide-file (str tmp_dir "write_node_test_graph/node")))
    ; (is (= (persist/get-contents node_file) "a|b^"))
    ; (persist/write-node "write_node_test_graph" {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {}} tmp_dir)
    ; (persist/read-from-disk node_file)
    ; (is (= (persist/get-contents node_file) "a|b^w|x@y|z^"))))

; (deftest write-edge-test
;   (with-tmp-dir
;     (persist/create-graph "create_edge_test_graph" tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "a" :descriptor_set #{"b"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "c" :descriptor_set #{"d"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "e" :descriptor_set #{"f"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "g" :descriptor_set #{"h"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "i" :descriptor_set #{"k"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-node "create_edge_test_graph" {:name "j" :descriptor_set #{"l"} :attribute_map {} :adjacency_map {}} tmp_dir)
;     (persist/write-edge "create_edge_test_graph" {:u "a" :label {:label "foo"} :v "c"
;                                                    :direction "--"} tmp_dir)
;     (persist/write-edge "create_edge_test_graph" {:u "e" :label {:label "bar"} :v "g"
;                                                    :direction "->"} tmp_dir)
;     (persist/write-edge "create_edge_test_graph" {:u "i" :label {:label "baz"} :v "j"
;                                                    :direction "<-"} tmp_dir)
;     (def edge_file (persist/provide-file (str tmp_dir "create_edge_test_graph/edge")))
;     (is (= (persist/get-contents edge_file) "a|-|foo|-|c^e|-|bar|>|g^i|<|baz|-|j^"))))

(deftest write-graph-test
  (with-tmp-dir
    (persist/create-graph "write_graph_test_graph" tmp_dir)
    (persist/write-graph {:name "write_graph_test_graph" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "foo"}}}}
                                                                 :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                                 :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "bar"} {:label "baz"}}}}}} tmp_dir)
    (def graph_file (persist/provide-file (str tmp_dir "write_graph_test_graph/graph.fress")))
    (is (= (persist/read-graph "write_graph_test_graph" tmp_dir) {:name "write_graph_test_graph" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "foo"}}}}
                                                                                                         :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                                                                         :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "bar"} {:label "baz"}}}}}}))))

(deftest delete-graph-test
  (with-tmp-dir
    (persist/create-graph "delete_graph_test_graph" tmp_dir)
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "delete_graph_test_graph/graph.fress")))))
    (persist/delete-graph "delete_graph_test_graph" tmp_dir)
    (is (false? (.exists (clojure.java.io/file (str tmp_dir "delete_graph_test_graph/node.fress")))))))

(deftest read-graph-test
  (with-tmp-dir
    (persist/write-graph {:name "read_graph_test_graph1" :nodes {:a {:name "a" :descriptor_set #{} :attribute_map {} :adjacency_map {:w #{{:label "con"}}}}
                                                                 :w {:name "w" :descriptor_set #{} :attribute_map {} :adjacency_map {:a #{{:label "con"}}}}}} tmp_dir)
    (persist/write-graph {:name "read_graph_test_graph2" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "con"}}}}
                                                                 :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "con"}}}}}} tmp_dir)
    (persist/write-graph {:name "read_graph_test_graph3" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "baz"}}}}
                                                                 :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "con"}}}}}} tmp_dir)
    (persist/write-graph {:name "read_graph_test_graph4" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "foo"}}}}
                                                                 :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                                 :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "bar"} {:label "baz"}}}}}} tmp_dir)
    (is (= (:nodes (persist/read-graph "read_graph_test_graph1" tmp_dir)) {:a {:name "a" :descriptor_set #{} :attribute_map {} :adjacency_map {:w #{{:label "con"}}}}
                                                                           :w {:name "w" :descriptor_set #{} :attribute_map {} :adjacency_map {:a #{{:label "con"}}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph2" tmp_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "con"}}}}
                                                                           :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "con"}}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph3" tmp_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "baz"}}}}
                                                                           :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "con"}}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph4" tmp_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{{:label "foo"}}}}
                                                                           :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                                           :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{{:label "bar"} {:label "baz"}}}}}))))