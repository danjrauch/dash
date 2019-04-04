(ns dash.query.parse
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]
            [clj-time.core :as t]
            [clj-time.local :as l]))

(defn parse-node-to-string
  ""
  [block]
  (def subject_attribute_split (str/split (subs block 1 (dec (count block))) #"\s{1,}"))
  (def name_adjs (map str/trim (str/split (nth subject_attribute_split 0) #"\:")))
  (if (= (count subject_attribute_split) 1)
    (str/join "|" name_adjs)
    (do
      (def attribute_string (subs (nth subject_attribute_split 1) 1 (dec (count (nth subject_attribute_split 1)))))
      (def attribute_pairs (remove empty? (map str/trim (str/split attribute_string #"(\s*:\s*|\s*,\s*|\s{1,})"))))
      (str (str/join "|" name_adjs) "@" (str/join "|" attribute_pairs)))))

(defn parse-edge-to-string
  ""
  [block]
  (str/join "|" (remove empty? (str/split block #"\(|\)|\[|\]"))))