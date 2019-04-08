(ns mesh.core.persist
  (:require [clojure.string :as str]
            [clojure.core.async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [clojure.data.fressian :as fress]
            [clojure.java.io :as io]
            [environ.core :as environ]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [mesh.core.graph :as graph])
  (:import [org.fressian Writer Reader StreamingWriter]
           [org.fressian.handlers ReadHandler WriteHandler]
           [java.nio ByteBuffer]))

(load "io")

(defn create-graph
  "Create a graph on disk."
  [graph_name data_dir]
  (create-path (str data_dir graph_name))
  (provide-file (str data_dir graph_name "/graph.fress"))
  (fress/write (str data_dir graph_name "/graph.fress"))
  ; (provide-file (str data_dir graph_name "/stats"))
  {:name graph_name :nodes {}})

(defn delete-graph
  "Delete a graph on disk."
  [graph_name data_dir]
  (delete-file-recursively (str data_dir graph_name)))

(defn read-graph
  "Reads a graph from a file."
  [graph_name data_dir]
  (def graph_file (provide-file (str data_dir graph_name "/graph.fress")))
  (if (> (count (get-contents graph_file)) 0)
    (fress/read (get-contents graph_file) :handlers read-handler-lookup)
    {:name graph_name :nodes {}}))

(defn write-graph
  "Writes a graph to a file."
  [graph data_dir]
  (def graph_file (provide-file (str data_dir (:name graph) "/graph.fress")))
  (set-contents graph_file (.array (fress/write graph :handlers write-handler-lookup)))
  (write-to-disk graph_file))

(defn read-graph-names
  "Read a graph_names file."
  [data_dir]
  (def graph_name_file (provide-file (str data_dir "graph_names")))
  (def graph_name_file_contents (bytes->string (get-contents graph_name_file)))
  (def graph_name_list (remove empty? (str/split-lines graph_name_file_contents)))
  (set graph_name_list))