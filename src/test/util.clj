(ns test.util
  (:require [mesh.persist :as persist]
            [mesh.repl :as repl]))

(def tmp_dir "test/test_data/")

(def public_dir "test_data/")

(def ^:dynamic *last-modified*)

(defn create-file-and-dirs
  {:added "0.1.0"}
  [path contents]
  (let [full_path (str tmp_dir path)]
    (.mkdirs (.getParentFile (clojure.java.io/file full_path)))
    (spit full_path contents)))

(defn delete-file-recursively
  {:added "0.1.0"}
  [f]
  (let [f (clojure.java.io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child)))
    (clojure.java.io/delete-file f)))

(defn clear-side-effects
  {:added "0.1.0"}
  []
  (persist/clear-files persist/BM)
  (reset! repl/global_graph {:name "" :nodes {}}))

(defmacro with-tmp-dir
  {:added "0.1.0"}
  [& body]
  `(do
     (.mkdirs (clojure.java.io/file tmp_dir))
     (let [result# (do ~@body)]
       (clear-side-effects)
       (delete-file-recursively tmp_dir)
       result#)))

(defmacro with-files
  {:added "0.1.0"}
  [files & body]
  `(do
     (binding [*last-modified* (* (.intValue (/ (System/currentTimeMillis) 1000)) 1000)]
       (doseq [[path# contents#] ~files] (create-file-and-dirs path# contents#))
       (let [result# (do ~@body)]
         (clear-side-effects)
         (when (.exists (clojure.java.io/file tmp_dir))
           (delete-file-recursively tmp_dir))
         result#))))