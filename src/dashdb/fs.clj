;;;; File read/write system for graph directory files.
;;;; Using a strategy called TAIN, or transactions as inodes.

(ns dashdb.fs
  (:require [clojure.java [io :as io]]))

(defn read-from-file
  "Read file into byte array and return the byte array."
  [{:keys [name] :as file}]
  (locking file
    (let [f (java.io.File. name)
          ary (byte-array (.length f))
          is (java.io.FileInputStream. f)]
      (.read is ary)
      (.close is)
      (assoc file :contents (vec ary)))))

(defn append-content
  "Write content(string) to the file map and return that map."
  [file contents]
  (locking file
    (update-in file [:contents] #(vec (concat % (map byte contents))))))

(defn write-to-file
  "Write byte array to file."
  [{:keys [name contents] :as file}]
  (locking file
    (let [f (java.io.File. name)
          is (java.io.FileOutputStream. f)]
      (.write is (byte-array contents))
      (.close is))))
