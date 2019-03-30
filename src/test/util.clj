(ns test.util
  (:require [dash.persistence.io :as io]))

(def tmp_dir "test/test_data")

(def public_dir "test_data")

(def ^:dynamic *last-modified*)

(defn create-file-and-dirs [path contents]
  (let [full_path (str tmp_dir path)]
    (.mkdirs (.getParentFile (clojure.java.io/file full_path)))
    (spit full_path contents)))

(defn delete-file-recursively [f]
  (let [f (clojure.java.io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child)))
    (clojure.java.io/delete-file f)))

(defmacro with-tmp-dir [& body]
  `(do
     (.mkdirs (clojure.java.io/file tmp_dir))
     (io/clear-files io/BM)
     (let [result# (do ~@body)]
       (delete-file-recursively tmp_dir)
       result#)))

(defmacro with-files [files & body]
  `(do
     (binding [*last-modified* (* (.intValue (/ (System/currentTimeMillis) 1000)) 1000)]
       (doseq [[path# contents#] ~files] (create-file-and-dirs path# contents#))
       (io/clear-files io/BM)
       (let [result# (do ~@body)]
         (when (.exists (clojure.java.io/file tmp_dir))
           (delete-file-recursively tmp_dir))
         result#))))