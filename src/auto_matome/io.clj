(ns auto-matome.io
  (:require [clojure.string :as str])
  (:use [clojure.java.io])
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
