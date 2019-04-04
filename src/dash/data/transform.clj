(ns dash.data.transform
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.local :as l]))

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

(defn string->node
  [data]
  (def node (str/split data #"\@"))
  (def name_and_descriptors (str/split (nth node 0) #"\|"))
  (if (= (count node) 1)
    {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors)) :adjacency_map {}}
    (do
      (def attributes (str/split (nth node 1) #"\|"))
      {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors))
       :attribute_map (zipmap (map keyword (take-nth 2 attributes)) (take-nth 2 (rest attributes)))
       :adjacency_map {}})))

(defn string->edge
  [data]
  (def edge_components (str/split data #"\|"))
  {:u (nth edge_components 0) :label (nth edge_components 2) :v (nth edge_components 4)
   :direction (str (nth edge_components 1) (nth edge_components 3))})