(in-ns 'mesh.graph)

(defn standard-datetime
  "Get the current datetime in UTC"
  {:added "0.1.0"}
  []
  (t/to-time-zone (l/local-now) t/utc))

(defn string->node
  {:added "0.1.0"}
  [data]
  (def node (str/split data #"\@"))
  (def name_and_descriptors (str/split (nth node 0) #"\|"))
  (if (= (count node) 1)
    {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors)) :attribute_map {} :adjacency_map {}}
    (do
      (def attributes (str/split (nth node 1) #"\|"))
      {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors))
       :attribute_map (zipmap (map keyword (take-nth 2 attributes)) (take-nth 2 (rest attributes)))
       :adjacency_map {}})))

(defn string->edge
  {:added "0.1.0"}
  [data]
  (def edge_components (str/split data #"\|"))
  (merge {:u (nth edge_components 0) :v (nth edge_components 4)
          :direction (str (nth edge_components 1) (nth edge_components 3))}
         (if (= (count (str/split (nth edge_components 2) #":")) 2)
           {:label (str (clojure.edn/read-string (nth (str/split (nth edge_components 2) #":") 0)))
            :w (clojure.edn/read-string (nth (str/split (nth edge_components 2) #":") 1))}
           (if (= (type (clojure.edn/read-string (nth edge_components 2))) clojure.lang.Symbol)
             {:label (str (clojure.edn/read-string (nth edge_components 2)))}
             {:w (clojure.edn/read-string (nth edge_components 2))}))))