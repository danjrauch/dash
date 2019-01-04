;;;; File read/write system for graph directory files.
;;;; Using transactions as inodes.

(ns dashdb.persistence.io
  (:require [clojure.string :as str]
            [clojure.core.async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [clj-time.local :as l]
            [environ.core :as environ]))

(defprotocol File
  (get-file-name [this])
  (get-file-contents [this])
  (set-file-name [this nname])
  (read-from-file [this])
  (write-to-file [this])
  (append-content [this contents])
  (concat-append [this args]))

(defn bytes->string
  [data]
  (apply str (map char data)))

(defn bytes->num
  [data]
  (reduce bit-or (map-indexed (fn [i x] (bit-shift-left (bit-and x 0x0FF) (* 8 (- (count data) i 1)))) data)))

(defn create-log
  "I/O logs for file operations."
  []
  (let [in (chan)]
    (go (loop []
          (let [op (<! in)
                env (environ/env :clj-env)
                ci (System/getenv "CONTINUOUS_INTEGRATION")]
            (case env
              "test"   (if (not ci) (spit "data/test_log" (str op "\n") :append true))
                       (spit "data/log" (str op "\n") :append true))
            (recur))))
  in))

(defn create-file
  "Create a new File"
  [name]
  (let [file (java.util.HashMap. {:name name :contents (java.util.ArrayList.) :in (create-log)})]
    (reify
      File
      (get-file-name [_] (.get ^java.util.HashMap file :name))
      (get-file-contents [_]
        (locking file
          (vec (.get ^java.util.HashMap file :contents))))
      (set-file-name [_ nname] (.put ^java.util.HashMap file :name nname))
      (read-from-file [_]
        (locking file
          (let [f (java.io.File. (.get ^java.util.HashMap file :name))
                ary (byte-array (.length f))
                is (java.io.FileInputStream. f)]
            (.read is ary)
            (.close is)
            (.put ^java.util.HashMap file :contents (java.util.ArrayList. (vec ary))))))
      (append-content [_ contents]
        (locking file
          (.addAll (.get ^java.util.HashMap file :contents) (java.util.ArrayList. (vec (map byte contents))))))
      (concat-append [this args]
        (locking file
          (>!! (:in file) (str (l/local-now) "|Started writing contents to virtual file|" (:name file) "|" (bytes->string (str/join " " args))))
          (append-content this (str/join "|" args))
          (>!! (:in file) (str (l/local-now) "|Done writing contents to virtual file|" (:name file) "|" (bytes->string (str/join " " args))))))
      (write-to-file [_]
        (locking file
          (let [f (java.io.File. (:name file))
                is (java.io.FileOutputStream. f)]
            (>!! (:in file) (str (l/local-now) "|Started writing to actual file|" (:name file)))
            (.write is (byte-array (.get ^java.util.HashMap file :contents)))
            (.close is)
            (>!! (:in file) (str (l/local-now) "|Done writing to actual file|" (:name file)))))))))
