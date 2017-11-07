(ns auto-matome.io
  (:require [clojure.string :as str])
  (:use [clojure.java.io]
        [auto-matome.thread])
  (:import (java.io PrintWriter)
           (java.io FileInputStream)))

(defn write-strings
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (println line)
      (if (= line "\n")
        nil
        (.write w line)))))

(defn write-strings-line
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (println line)
      (.write w (str/join [line "\n"])))))

(defn write-csv
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (.write w (str/join [line ","])))))

(defn read-csv
  [file-path]
  (println file-path)
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (println line)
      (if (nil? line)
        result
        (recur (.readLine r) (conj (str/split line #",") line))))))


(defn read-contents
  [file-path]
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (if (nil? line)
        result
        (recur (.readLine r) (conj result line))))))

(defn read-csv-responses
  [file-path]
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (if (nil? line)
        result
          (recur (.readLine r) (conj result (csv-to-response line)))
          ))))

(defn record-dictionary
  [word-index-maps file-path]
  (let [dic-strs (map #(str/join [(:word %) "," (:index %)]) word-index-maps)]
    (write-strings-line dic-strs file-path)))

(defn read-dictionary
  [file-path]
  (let [lines (read-contents file-path)
        word-index-list(map #(str/split % #",") lines)]
    (map (fn [wi] {:word (first wi) :index (second wi)}) word-index-list)))

(defn record-id-dictionary
  [id-index-maps file-path]
  (let [dic-strs (map #(str/join [(:id %) "," (:index %)]) id-index-maps)]
    (write-strings-line dic-strs file-path)))

(defn read-id-dictionary
  [file-path]
  (let [lines (read-contents file-path)
        id-index-list(map #(str/split % #",") lines)]
    (map (fn [ii] {:id (first ii) :index (second ii)}) id-index-list)))

(defn read-responses-with-words
  [file-path]
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (if (nil? line)
        result
        (recur (.readLine r) (conj result (csv-to-response-with-words line)))))))

(defn record-original-and-matome-urls
  [original-and-matome-urls file-path]
  (let [joined-strs (doall (map #(str/join "," %) original-and-matome-urls))]
    (write-strings-line joined-strs  file-path)))

(defn read-original-and-matome-urls
  [file-path]
  (let [lines (read-contents file-path)]
    (doall (map #(str/split % #",") lines)))
  )

(defn record-vectors-with-labels
  [padded-vectors labels file-path]
  (println "record-vectors-with-labels")
  (println labels)
  (println padded-vectors)
  (let [nested-labels-vectors (doall (map vector labels padded-vectors))
        labels-vectors (doall (pmap #(flatten %) nested-labels-vectors))
        string-labels-vectors (doall (map (fn [x] (map (fn [y] (str y)) x)) labels-vectors))
        csv-strings (doall (map (fn [vl] (str/join "," vl)) string-labels-vectors))]
    (write-strings-line csv-strings file-path)
    ))

(defn read-vectors-with-labels
  [file-path]
  (let [lines (read-contents file-path)]
    (doall (pmap #(str/split % #",") lines))
    ))

(defn record-vectors
  [vecs file-path]
  (let [string-vecs (mapv (fn [x] (map (fn [y] (str y)) x)) vecs)
        csv-strings (doall (map (fn [vl] (str/join "," vl)) string-vecs))]
    (write-strings-line csv-strings file-path)    
    ))

(defn csv-to-normalized-data
  [csv-string]
  (let [splitted (str/split csv-string #",")]
    {:label (Integer. (first splitted))
     :data (mapv #(Double. %) (rest splitted))}
    ))

(defn read-normalized-data
  [file-path]
  (let [lines (read-contents file-path)]
    (doall (pmap #(csv-to-normalized-data %) lines))
    ))
