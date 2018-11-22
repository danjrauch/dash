(ns dashdb.file)

(defrecord File [name type])

(defn createFile "Create a file"
  [name type] (File. name type))

(defn writeFile "Append content to a file"
  [file content] (locking file (spit (:name file) content :append true)))

(defn transfer_content "Transfer content to a file"
  [file i] (writeFile file (Integer/toHexString i)))