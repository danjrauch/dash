;;;; File system implementing transations as inodes, or TAIN

(ns dashdb.file
  (:require [clojure.java [io :as io]]))

(defn write-file
  "Append content to a file."
  [{:keys [name content] :as file}]
  (locking file (spit name content :append true)))

(defn get-file
  "Gets the contents of a file. Will eventually be abandoned."
  [{:keys [name] :as file}]
  (locking file (slurp name)))

(defn file-bytes
  "Copies contents of a file to xout java.io.ByteArrayOutputStream and then outputs a java ByteArray. Too slow because of io/copy obviously."
  [{:keys [name] :as file}]
  (locking file
    (with-open [xin (io/input-stream name)
                xout (java.io.ByteArrayOutputStream.)]
      (io/copy xin xout)
      (.toByteArray xout))))

(defn read-nth-block
  "Read line-number from the given text file. The first line has the number 1. Too slow because of lazy seq creating by line-seq"
  [{:keys [name number] :as file}]
  (with-open [rdr (clojure.java.io/reader name)]
    (nth (line-seq rdr) (dec number))))
