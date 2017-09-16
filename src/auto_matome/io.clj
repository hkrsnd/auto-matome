(ns auto-matome.io
  (:use [clojure.java.io])
  (:import (java.io PrintWriter)
           (java.io FileInputStream))
  )

(defn write-strings
  [strs]
  (with-open [w (writer "resource/contents.txt")]
    (doseq [line strs]
      (.write w line)
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
