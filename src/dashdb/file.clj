(ns dashdb.file
  (:require [clojure.java [io :as io]]))

(defrecord File [name type])

(defn createFile
  "Create a file record."
  [name type]
  (File. name type))

(defn writeFile
  "Append content to a file."
  [file content]
  (locking file (spit (:name file) content :append true)))

(defn getFile
  "Gets the contents of a file. Will eventually be abandoned."
  [file]
  (locking file (slurp (:name file))))

(defn transfer_content
  "Transfer hex content to the end of a blockfile."
  [file i]
  (writeFile file (Integer/toHexString i)))

(defn get_content_at_block
  "Gets the contents of a block on disk. Too slow because of full file copy by getFile."
  [file i size]
  (subs (getFile file) (* (- i 1) size) (* i size)))

(defn file->bytes
  "Copies contents of a file to xout java.io.ByteArrayOutputStream and then outputs a java ByteArray. Too slow because of io/copy obviously."
  [file]
  (locking file
    (with-open [xin (io/input-stream (:name file))
                xout (java.io.ByteArrayOutputStream.)]
      (io/copy xin xout)
      (.toByteArray xout))))

(defn read_nth_block
  "Read line-number from the given text file. The first line has the number 1. Too slow because of lazy seq creating by line-seq"
  [file line_number]
  (with-open [rdr (clojure.java.io/reader (:name file))]
    (nth (line-seq rdr) (dec line_number))))
