(ns auto-matome.thread
  (:require [clojure.string :as str])
  )

(defstruct thread :url :responses)
(defstruct response :num :id :datetime :content)

(defn response-to-string
  [response]
  (str/join [(:num response) "," (:id response) "," (:datetime response) "," (:content response)])
  )
