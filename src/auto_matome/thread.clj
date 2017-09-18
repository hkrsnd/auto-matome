(ns auto-matome.thread
  (:require [clojure.string :as str])
  )

(defstruct thread :url :responses)
(defstruct response :num :id :datetime :target :content)

(defn response-to-string
  [response]
  (str/join [(:num response) "," (:id response) "," (:datetime response) "," (:target response) "," (:content response)])
  )

(defn csv-to-response
  [csv-string]
  (let [sp (str/split csv-string #",")
        rest (subvec sp 4)
        content (reduce #(str/join %) rest)]
    (struct response
            (nth sp 0)
            (nth sp 1)
            (nth sp 2)
            (nth sp 3)
            content
            )
    )
  )
