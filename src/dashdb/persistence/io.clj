;;;; File read/write system for graph directory files.
;;;; Using a strategy called TAIN, or transactions as inodes.

(ns dashdb.persistence.io
  (:require [clojure.string :as str]))

(defn bytes->num 
  [data]
  (reduce bit-or (map-indexed (fn [i x] (bit-shift-left (bit-and x 0x0FF) (* 8 (- (count data) i 1)))) data)))

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

(defn concat-content
  "Concat the contents of a record into dash format."
  [& args]
  (str/join args))

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
