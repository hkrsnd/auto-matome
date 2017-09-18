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

(defn write-words
  [strs file-path]
  (with-open [w (writer file-path)]
    (doseq [line strs]
      (.write w (str/join [line ","]))
      ))
  )

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
  [word-and-index-list file-path]
  (let [dic-strs (map #(str/join [(first %) "," (second %)]) word-and-index-list)]
    (write-strings-line dic-strs file-path)
    ))

(defn read-dictionary
  [file-path]
  (let [lines (read-contents file-path)]
    (map #(str/split % #",") lines)
  ))
