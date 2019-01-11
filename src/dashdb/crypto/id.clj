(ns dashdb.crypto.id)

(defn create-id
  "Create an entity ID"
  []
  (loop [id ""
         l 10]
    (if (= l 0)
      id
      (recur (str id (char (+ 65 (rand-int 26)))) (dec l)))))
