(ns auto-matome.thread
  (:require [clojure.string :as str])
  )

(defstruct thread :url :responses)
(defstruct response :num :id :datetime :target :content)

(defn remove-newline
  [string]
  (try
    (str/join (filter #(not= "\n" %) (str/split string #"")))
    (catch Exception e nil)))

(defn response-to-string
  [response]
  (let [num-str (:num response)
        id-str (:id response)
        datetime-str (:datetime response)
        target-str (:target response)
        ;content-str (:content response)
        content-str (str/join [(remove-newline (:content response)) "\n"])
                                        ;content-str (str/join [content-str-filterd "\n"])
        ]
    (println content-str)
    (str/join "," [num-str id-str datetime-str target-str content-str])
    )
;    (str/join [(:num response) "," (:id response) "," (:datetime response) "," (:target response) "," (:content response)])
  )

(defn csv-to-response
  [csv-string]
    (let [sp (str/split csv-string #",")
          rest (subvec sp 4)
          content (str/join "," rest)]
      (struct response
            (nth sp 0)
            (nth sp 1)
            (nth sp 2)
            (nth sp 3)
            content
            )))


(defn csv-to-response-with-words
  [csv-string]
  (let [sp (str/split csv-string #",")
        words (str/split (last sp) #";")]
    (struct response
            (nth sp 0)
            (nth sp 1)
            (nth sp 2)
            (nth sp 3)
            words
            )))
