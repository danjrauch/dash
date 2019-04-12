(in-ns 'mesh.core.persist)

(defn connect-log
  "I/O logs for file operations."
  {:added "0.1.0"}
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

(defn bytes->string
  {:added "0.1.0"}
  [data]
  (apply str (map char data)))

(defn bytes->num
  {:added "0.1.0"}
  [data]
  (reduce bit-or (map-indexed (fn [i x] (bit-shift-left (bit-and x 0x0FF) (* 8 (- (count data) i 1)))) data)))

(defprotocol File
  (get-name [this])
  (get-contents [this])
  (get-r-value [this])
  (get-dirty-value [this])
  (set-name [this nname])
  (set-contents [this contents])
  (set-r-value [this nr])
  (set-dirty-value [this nd])
  (append-string [this contents])
  (concat-append-string [this args separator])
  (read-from-disk [this])
  (write-to-disk [this]))

(defprotocol BManager
  (get-file-position [this name])
  (get-file [this name])
  (get-files [this])
  (clear-files [this])
  (pin-file [this name])
  (unpin-file [this name])
  (write-files-to-disk [this]))

(defn create-file
  "Create a new File structure"
  {:added "0.1.0"}
  [name]
  (let [file (atom {:name name :contents (byte-array 0)
                    :dirty false :r 0 :pivot 0 :in (connect-log)})]
    (reify
      File
      (get-name [_]
        (:name @file))
      (get-contents [_]
        (:contents @file))
      (get-r-value [_]
        (:r @file))
      (get-dirty-value [_]
        (:dirty @file))
      (set-name [_ nname]
        (swap! file assoc :name nname))
      (set-contents [_ contents]
        (swap! file assoc :contents (byte-array contents)))
      (set-r-value [_ nr]
        (swap! file assoc :r nr))
      (set-dirty-value [_ nd]
        (swap! file assoc :dirty nd))
      (append-string [this contents]
        (swap! file update :contents #(byte-array (concat % (byte-array (map byte contents)))))
        (set-dirty-value this true)
        contents)
      (concat-append-string [this args separator]
        (append-string this (str/join separator args)))
      (read-from-disk [_]
        (when (not (.exists (io/file (:name @file))))
          (io/make-parents (:name @file))
          (.createNewFile (io/file (:name @file))))
        (let [f (java.io.File. (:name @file))
              ary (byte-array (.length f))
              is (java.io.FileInputStream. f)]
          (.read is ary)
          (.close is)
          (swap! file assoc :contents ary)))
      (write-to-disk [_]
        (when (not (.exists (io/file (:name @file))))
          (io/make-parents (:name @file))
          (.createNewFile (io/file (:name @file))))
        (let [f (java.io.File. (:name @file))
              is (java.io.FileOutputStream. f false)]
          (.write is (:contents @file))
          (.close is))))))

(defn initialize-buffer-file-array
  {:added "0.1.0"}
  [size]
  (let [files (java.util.ArrayList.)]
    (doseq [_ (range size)] (.add files (create-file "No File"))) files))

(defn create-buffer-manager
  "Create a new buffer manager"
  {:added "0.1.0"}
  [size]
  (let [bmanager (java.util.HashMap. {:files (initialize-buffer-file-array size) :hand 0})]
    (reify
      BManager
      (get-file-position [_ name]
        (first (keep-indexed #(if (= name (get-name %2)) %1) (.get ^java.util.HashMap bmanager :files))))
      (get-file [_ name]
        (first (filter #(= name (get-name %)) (.get ^java.util.HashMap bmanager :files))))
      (get-files [_]
        (vec (.get ^java.util.HashMap bmanager :files)))
      (clear-files [_]
        (locking bmanager
          (doseq [file (.get ^java.util.HashMap bmanager :files)]
            (when (= (get-dirty-value file) true)
              (write-to-disk file)
              (set-dirty-value file false))
            (set-name file "No File"))))
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
                      (write-to-disk file)
                      (set-dirty-value file false))
                    (set-name file name)
                    (read-from-disk file)
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
            (when (= (get-dirty-value file-to-unpin) true)
              (write-to-disk file-to-unpin)
              (set-dirty-value file-to-unpin false))
            (set-r-value file-to-unpin 0)
            (set-name file-to-unpin "No File"))))
      (write-files-to-disk [_]
        (locking bmanager
          (doseq [file (.get ^java.util.HashMap bmanager :files)]
            (when (= (get-dirty-value file) true)
              (write-to-disk file)
              (set-dirty-value file false))))))))

(def BM (create-buffer-manager 10))

(defn provide-file
  ""
  {:added "0.1.0"}
  [path]
  (pin-file BM path)
  (get-file BM path))

(defn create-path
  ""
  {:added "0.1.0"}
  [path]
  (.mkdirs (io/file path)))

(defn delete-file-recursively
  "Delete file or a directory and everything inside."
  {:added "0.1.0"}
  [file_name]
  (let [file (clojure.java.io/file file_name)]
    (if (.isDirectory file)
      (doseq [child (.listFiles file)]
        (delete-file-recursively child)))
    (unpin-file BM file_name)
    (clojure.java.io/delete-file file)))

;; Custom Fressian Handlers

(defn write-with-meta
  "Writes the object to the writer under the given tag.  If the record has metadata, the metadata
   will also be written. The optional `write-fn` will be used to write the object if given. If 
   not given, the default is Writer.writeList().
   `read-with-meta` (below) will associated this metadata back with the object when reading."
  {:added "0.1.0"}
  ([w tag o]
   (write-with-meta w tag o (fn [^Writer w o] (.writeList w o))))
  ([^Writer w tag o write-fn]
   (let [m (meta o)]
     (do
       (.writeTag w tag 2)
       (write-fn w o)
       (if m
         (.writeObject w m)
         (.writeNull w))))))

(defn- read-meta [^Reader rdr]
  {:added "0.1.0"}
  (some->> rdr
           .readObject
           (into {})))

(defn read-with-meta
  "Reads an object from the reader that was written via `write-with-meta` (above).  If the object
   was written with metadata the metadata will be associated on the object returned. The `build-fn`
   is called on the read object and is used to do any additional construnction necessary for 
   data structure."
  {:added "0.1.0"}
  [^Reader rdr build-fn]
  (let [o (build-fn (.readObject rdr))
        m (read-meta rdr)]
    (cond-> o
      m (with-meta m))))

(defn write-map
  "Writes a map as Fressian with the tag 'map' and all keys cached."
  {:added "0.1.0"}
  [^Writer w m]
  (.writeTag w "map" 1)
  (.beginClosedList ^StreamingWriter w)
  (reduce-kv
   (fn [^Writer w k v]
     (.writeObject w k true)
     (.writeObject w v))
   w
   m)
  (.endList ^StreamingWriter w))

(defn sorted-comparator-name
  "Sorted collections are not easily serializable since they have an opaque function object instance
   associated with them.  To deal with that, the sorted collection can provide a 
   :fressian.custom/comparator-name in the metadata that indicates a symbolic name for the function 
   used as the comparator.  With this name the function can be looked up and associated to the
   sorted collection again during deserialization time.
   * If the sorted collection metadata has a :fressian.custom/comparator-name, then the symbol value is returned.
   * If the sorted collection has the clojure.lang.RT/DEFAULT_COMPARATOR, returns nil.
   * If neither of the above are true, an exception is thrown indicating that there is no way to provide a useful
     name for this sorted collection, so it won't be able to be serialized."
  {:added "0.1.0"}
  [^clojure.lang.Sorted s]
  (let [cname (-> s meta :fressian.custom/comparator-name)]

    ;; Fail if reliable serialization of this sorted coll isn't possible.
    (when (and (not cname)
               (not= (.comparator s) clojure.lang.RT/DEFAULT_COMPARATOR))
      (throw (ex-info (str "Cannot serialize sorted collection with non-default"
                           " comparator because no :fressian.custom/comparator-name provided in metadata.")
                      {:sorted-coll s
                       :comparator (.comparator s)})))

    cname))

(defn seq->sorted-set
  "Helper to create a sorted set from a seq given an optional comparator."
  {:added "0.1.0"}
  [s ^java.util.Comparator c]
  (if c
    (clojure.lang.PersistentTreeSet/create c (seq s))
    (clojure.lang.PersistentTreeSet/create (seq s))))

(defn seq->sorted-map
  "Helper to create a sorted map from a seq given an optional comparator."
  {:added "0.1.0"}
  [s ^java.util.Comparator c]
  (if c
    (clojure.lang.PersistentTreeMap/create c ^clojure.lang.ISeq (sequence cat s))
    (clojure.lang.PersistentTreeMap/create ^clojure.lang.ISeq (sequence cat s))))

(def write-handlers
  {;; Persistent set
   clojure.lang.APersistentSet
   {"clj/set"
    (reify WriteHandler
      (write [_ w o]
        (write-with-meta w "clj/set" o)))}

   ;; Persistent sorted (tree) set
   {clojure.lang.PersistentTreeSet
    {"clj/treeset" (reify WriteHandler
                     (write [_ w o]
                       (let [cname (sorted-comparator-name o)]
                         (.writeTag w "clj/treeset" 3)
                         (if cname
                           (.writeObject w cname true)
                           (.writeNull w))
                         ;; Preserve metadata.
                         (if-let [m (meta o)]
                           (.writeObject w m)
                           (.writeNull w))
                         (.writeList w o))))}}

   ;; Persistent sorted (tree) map
   {clojure.lang.PersistentTreeMap
    {"clj/treemap" (reify WriteHandler
                     (write [_ w o]
                       (let [cname (sorted-comparator-name o)]
                         (.writeTag w "clj/treemap" 3)
                         (if cname
                           (.writeObject w cname true)
                           (.writeNull w))
                         ;; Preserve metadata.
                         (if-let [m (meta o)]
                           (.writeObject w m)
                           (.writeNull w))
                         (write-map w o))))}}

   ;; Persistent vector
   {clojure.lang.APersistentVector
    {"clj/vector" (reify WriteHandler
                    (write [_ w o]
                      (write-with-meta w "clj/vector" o)))}}

   ;; Persistent list
   ;; NOTE: The empty list is a different data type in Clojure and has to be handled separately.
   {clojure.lang.PersistentList
    {"clj/list" (reify WriteHandler
                  (write [_ w o]
                    (write-with-meta w "clj/list" o)))}}

   {clojure.lang.PersistentList$EmptyList
    {"clj/emptylist" (reify WriteHandler
                       (write [_ w o]
                         (let [m (meta o)]
                           (do
                             (.writeTag w "clj/emptylist" 1)
                             (if m
                               (.writeObject w m)
                               (.writeNull w))))))}}

   ;; Persistent seq & lazy seqs
   {clojure.lang.ASeq
    {"clj/aseq" (reify WriteHandler
                  (write [_ w o]
                    (write-with-meta w "clj/aseq" o)))}}

   {clojure.lang.LazySeq
    {"clj/lazyseq" (reify WriteHandler
                     (write [_ w o]
                       (write-with-meta w "clj/lazyseq" o)))}}


   ;; java.lang.Class type
   {Class
    {"java/class" (reify WriteHandler
                    (write [_ w c]
                      (.writeTag w "java/class" 1)
                      (.writeObject w (symbol (.getName ^Class c)) true)))}}})

(def read-handlers
  {;; Persistent set
   "clj/set" (reify ReadHandler
               (read [_ rdr tag component-count]
                 (read-with-meta rdr set)))

   ;; Persistent sorted (tree) set
   "clj/treeset" (reify ReadHandler
                   (read [_ rdr tag component-count]
                     (let [c (some-> rdr .readObject resolve deref)
                           m (.readObject rdr)
                           s (-> (.readObject rdr)
                                 (seq->sorted-set c))]
                       (if m
                         (with-meta s m)
                         s))))

   ;; Persistent sorted (tree) map
   "clj/treemap" (reify ReadHandler
                   (read [_ rdr tag component-count]
                     (let [c (some-> rdr .readObject resolve deref)
                           m (.readObject rdr)
                           s (seq->sorted-map (.readObject rdr) c)]
                       (if m
                         (with-meta s m)
                         s))))

   ;; Persistent vector
   "clj/vector" (reify ReadHandler
                  (read [_ rdr tag component-count]
                    (read-with-meta rdr vec)))

   ;; Persistent list
   ;; NOTE: The empty list is a different data type in Clojure and has to be handled separately.
   "clj/list" (reify ReadHandler
                (read [_ rdr tag component-count]
                  (read-with-meta rdr #(apply list %))))

   "clj/emptylist" (reify ReadHandler
                     (read [_ rdr tag component-count]
                       (let [m (read-meta rdr)]
                         (cond-> '()
                           m (with-meta m)))))

   ;; Persistent seq & lazy seqs
   "clj/aseq" (reify ReadHandler
                (read [_ rdr tag component-count]
                  (read-with-meta rdr sequence)))

   "clj/lazyseq" (reify ReadHandler
                   (read [_ rdr tag component-count]
                     (read-with-meta rdr sequence)))

   ;; java.lang.Class type
   "java/class" (reify ReadHandler
                  (read [_ rdr tag component-count]
                    (resolve (.readObject rdr))))})

(def write-handler-lookup
  (-> (merge write-handlers fress/clojure-write-handlers)
      fress/associative-lookup
      fress/inheritance-lookup))

(def read-handler-lookup
  (-> (merge read-handlers fress/clojure-read-handlers)
      fress/associative-lookup))