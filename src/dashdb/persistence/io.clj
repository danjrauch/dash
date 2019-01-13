(ns dashdb.persistence.io
  (:require [clojure.string :as str]
            [clojure.core.async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [environ.core :as environ]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clojure.java.io :as io]))

(defprotocol File
  (get-name [this])
  (get-contents [this])
  (get-r-value [this])
  (get-dirty-value [this])
  (set-name [this nname])
  (set-r-value [this nr])
  (set-dirty-value [this nd])
  (read-from-file [this])
  (write-to-file [this])
  (append-content [this contents])
  (concat-append [this args]))

(defprotocol BManager
  (get-file-position [this name])
  (get-file [this name])
  (pin-file [this name])
  (unpin-file [this name])
  (write-files-to-disk [this]))

(defn standard-datetime
  "Get the current datetime in UTC"
  []
  (t/to-time-zone (l/local-now) t/utc))

(defn bytes->string
  [data]
  (apply str (map char data)))

(defn bytes->num
  [data]
  (reduce bit-or (map-indexed (fn [i x] (bit-shift-left (bit-and x 0x0FF) (* 8 (- (count data) i 1)))) data)))

(defn connect-log
  "I/O logs for file operations."
  []
  (let [in (chan)]
    (go (loop []
          (let [op (<! in)
                env (environ/env :clj-env)
                ci (System/getenv "CONTINUOUS_INTEGRATION")]
            (case env
              "test"   (if-not ci (spit "data/test_log" (str op "\n") :append true))
                       (spit "data/log" (str op "\n") :append true))
            (recur))))
  in))

(defn create-file
  "Create a new File"
  [name]
  (let [file (java.util.HashMap. {:name name :contents (java.util.ArrayList.)
                                  :dirty false :r 0 :pivot 0 :in (connect-log)})]
    (reify
      File
      (get-name [_]
        (locking file
          (.get ^java.util.HashMap file :name)))
      (get-contents [_]
        (locking file
          (vec (.get ^java.util.HashMap file :contents))))
      (get-r-value [_]
        (locking file
          (.get ^java.util.HashMap file :r)))
      (get-dirty-value [_]
        (locking file
          (.get ^java.util.HashMap file :dirty)))
      (set-name [_ nname]
        (locking file
          (.put ^java.util.HashMap file :name nname)))
      (set-r-value [_ nr]
        (locking file
          (.put ^java.util.HashMap file :r nr)))
      (set-dirty-value [_ nd]
        (locking file
          (.put ^java.util.HashMap file :dirty nd)))
      (read-from-file [_]
        (locking file
          (when (not (.exists (io/file (.get ^java.util.HashMap file :name))))
            (.mkdirs (.getParentFile (io/file (.get ^java.util.HashMap file :name)))))
          (let [f (java.io.File. (.get ^java.util.HashMap file :name))
                ary (byte-array (.length f))
                is (java.io.FileInputStream. f)]
            (.read is ary)
            (.close is)
            (.put ^java.util.HashMap file :contents (java.util.ArrayList. (vec ary)))
            (.put ^java.util.HashMap file :pivot (.length f)))))
      (append-content [this contents]
        (locking file
          (.addAll (.get ^java.util.HashMap file :contents) (java.util.ArrayList. (vec (map byte contents))))
          (set-dirty-value this true)))
      (concat-append [this args]
        (locking file
          (>!! (.get ^java.util.HashMap file :in) (str (standard-datetime) "|SWVF|"
                               (.get ^java.util.HashMap file :name) "|" (bytes->string (str/join " " args))))
          (append-content this (str/join "|" args))
          (>!! (.get ^java.util.HashMap file :in) (str (standard-datetime) "|DWVF|"
                               (.get ^java.util.HashMap file :name) "|" (bytes->string (str/join " " args))))))
      (write-to-file [_]
        (locking file
          (when (not (.exists (io/file (.get ^java.util.HashMap file :name))))
            (.mkdirs (.getParentFile (io/file (.get ^java.util.HashMap file :name)))))
          (let [f (java.io.File. (.get ^java.util.HashMap file :name))
                is (java.io.FileOutputStream. f true)]
            (>!! (.get ^java.util.HashMap file :in) (str (standard-datetime) "|SWAF|"
                                                         (.get ^java.util.HashMap file :name)))
            (.write is (byte-array (.subList (.get ^java.util.HashMap file :contents)
                                             (.get ^java.util.HashMap file :pivot)
                                             (.size (.get ^java.util.HashMap file :contents)))))
            (.put ^java.util.HashMap file :pivot (.size (.get ^java.util.HashMap file :contents)))
            (.close is)
            (>!! (.get ^java.util.HashMap file :in) (str (standard-datetime) "|DWAF|"
                                                         (.get ^java.util.HashMap file :name)))))))))

(defn initialize-buffer-file-array
  [size]
  (let [files (java.util.ArrayList.)]
    (doseq [_ (range size)] (.add files (create-file "No File"))) files))

(defn create-buffer-manager
  "Create a new buffer manager"
  [size]
  (let [bmanager (java.util.HashMap. {:files (initialize-buffer-file-array size) :hand 0})]
  (reify
    BManager
    (get-file-position [_ name]
      (first (keep-indexed #(if (= name (get-name %2)) %1) (.get ^java.util.HashMap bmanager :files))))
    (get-file [this name]
      (first (filter #(= name (get-name %)) (.get ^java.util.HashMap bmanager :files))))
    (pin-file [this name]
      (locking bmanager
        (def file-to-pin (get-file this name))
        (when (not (nil? file-to-pin))
          (set-r-value file-to-pin 1))
        (when (nil? file-to-pin)
          (loop [file (.get (.get ^java.util.HashMap bmanager :files) (.get ^java.util.HashMap bmanager :hand))]
            (case (get-r-value file)
                  0 (do
                      (when (= (get-dirty-value file) true)
                        (write-to-file file)
                        (set-dirty-value file false))
                      (set-name file name)
                      (read-from-file file)
                      (set-r-value file 1)
                      (.put ^java.util.HashMap bmanager :hand (mod (inc (.get ^java.util.HashMap bmanager :hand)) size)))
                  1 (do
                      (set-r-value file 0)
                      (.put ^java.util.HashMap bmanager :hand (mod (inc (.get ^java.util.HashMap bmanager :hand)) size))
                      (recur (.get (.get ^java.util.HashMap bmanager :files) (.get ^java.util.HashMap bmanager :hand)))))))))
    (unpin-file [this name]
      (locking bmanager
        (def file-to-unpin (get-file this name))
        (when (not (nil? file-to-unpin))
          (set-r-value file-to-unpin 0)
          (when (= (get-dirty-value file-to-unpin) true)
            (write-to-file file-to-unpin)
            (set-dirty-value file-to-unpin false)))))
    (write-files-to-disk [_]
      (locking bmanager
        (doseq [x (.get ^java.util.HashMap bmanager :files)]
          (when (= (get-dirty-value x) true)
            (write-to-file x)
            (set-dirty-value x false))))))))

(def BM (create-buffer-manager 10))