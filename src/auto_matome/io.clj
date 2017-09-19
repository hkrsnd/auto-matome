(ns auto-matome.io
  (:require [clojure.string :as str])
  (:use [clojure.java.io]
        [auto-matome.thread])
  (:import (java.io PrintWriter)
           (java.io FileInputStream))
  )

(defn write-strings
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (.write w line)
      ))
  )

(defn write-strings-line
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (.write w (str/join [line "\n"]))
      ))
  )

(defn write-csv
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (.write w (str/join [line ","]))
      )))

(defn read-contents
  [file-path]
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (if (nil? line)
        result
        (recur (.readLine r) (conj result line))))
    ))

(defn read-csv-responses
  [file-path]
  (with-open [r (reader file-path)]
    (loop [line (.readLine r)
           result []]
      (if (nil? line)
        result
        (recur (.readLine r) (conj result (csv-to-response line)))))
    ))

(defn record-dictionary
  [word-index-maps file-path]
  (let [dic-strs (map #(str/join [(:word %) "," (:index %)]) word-index-maps)]
    (write-strings-line dic-strs file-path)
    ))

(defn read-dictionary
  [file-path]
  (let [lines (read-contents file-path)
        word-index-list(map #(str/split % #",") lines)]
    (map (fn [wi] {:word (first wi) :index (second wi)}) word-index-list)
    ))

(defn record-id-dictionary
  [id-index-maps file-path]
  (let [dic-strs (map #(str/join [(:id %) "," (:index %)]) id-index-maps)]
    (write-strings-line dic-strs file-path)
    ))

(defn read-id-dictionary
  [file-path]
  (let [lines (read-contents file-path)
        id-index-list(map #(str/split % #",") lines)]
    (map (fn [ii] {:id (first ii) :index (second ii)}) id-index-list)
    ))
