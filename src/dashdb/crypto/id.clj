(ns dashdb.crypto.id)

(defn create-id
  "Create an entity ID"
  [length]
  (loop [id ""
         l length]
    (if (= l 0)
      id
      (recur (str id (char (rand-int 256))) (dec l)))))
