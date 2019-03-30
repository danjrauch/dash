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
