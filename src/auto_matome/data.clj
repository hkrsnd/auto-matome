(ns auto-matome.data
  (:require [clojure.string :as str])
  (:use [auto-matome.thread]
        [auto-matome.morpho]
        [auto-matome.regex]))


(defn zipped-vector-to-map
  [zipped-vec]
  (loop [result {} tmp-zipped zipped-vec]
    (let [zip (first tmp-zipped)]
      (if (empty? zip)
        result
        (recur (assoc result (first zip) (second zip)) (rest tmp-zipped))))))

(defn from-set-to-dictionary
  [words-set]
  (let [zipped (map-indexed #(vector %2 %1) words-set)]
    (map (fn [z] {:word (first z) :index (second z)}) zipped)
    ))

(defn from-set-to-id-dictionary
  [id-set]
  (let [zipped (map-indexed #(vector %2 %1) id-set)]
    (map (fn [z] {:id (first z) :index (second z)}) zipped)
    ))

(defn search-dictionary-by-word
  [word dictionary]
  (:index
   (first
    (filter #(= word (:word %)) dictionary))))

(defn search-dictionary-by-id
  [id dictionary]
  (:index
   (first
    (filter #(= id (:id %)) dictionary))))

(defn text-to-words
  [text]
  (let [analyzed (morphological-analysis-sentence text)]
    (doall (pmap #(first %) analyzed))
    ))

(defn words-to-vector
  [words dictionary]
  (doall (pmap (fn [word]
          (search-dictionary-by-word word dictionary)
          ) words)))

(defn num-to-vector
  [num]
  [num])

(defn datetime-to-vector
  [datetime]
  (let [re-datetime #"([0-9]+)/([0-9]+)/([0-9]+)-([0-9]+):([0-9]+):([0-9]+)\.[0-9]+"
        fined (re-find-ex re-datetime datetime)]
    (rest fined)
    ))

(defn id-to-vector
  [id dictionary]
  [(search-dictionary-by-id id dictionary)])

(defn target-to-vector
  [target]
  (if (= target "nil")
    ["0"]
    [target]
    ))

; "10"->10, "09"->9
(defn parse-int [s]
  (try
    (Integer. (re-find  #"\d+" s ))
    (catch Exception e 0)
    ))

(defn response-to-vector
  [response dictionary id-dictionary]
  ;;todo
  (let [num-vec (num-to-vector (:num response))
        id-vec (id-to-vector (:id response) id-dictionary)
        datetime-vec (datetime-to-vector (:datetime response))
        target-vec (target-to-vector (:target response))
        content-vec (words-to-vector (text-to-words (:content response)) dictionary)
        ]
    (print "ToVector: ")
    (println response)
    (doall
     (pmap #(parse-int %)
           (flatten [num-vec id-vec datetime-vec target-vec content-vec])))
    )
  )

(defn response-with-words-to-vector
  [response dictionary id-dictionary]
  ;;todo
  (let [num-vec (num-to-vector (:num response))
        id-vec (id-to-vector (:id response) id-dictionary)
        datetime-vec (datetime-to-vector (:datetime response))
        target-vec (target-to-vector (:target response))
        content-vec (words-to-vector  (:content response) dictionary)
        ]
    (print "ToVector: ")
    (println response)
    (doall
     (pmap #(parse-int %)
           (flatten [num-vec id-vec datetime-vec target-vec content-vec])))
    )
  )

(defn to-response-with-words
  [response]
  (assoc response :content (text-to-words (:content response)))
  )

(defn response-with-words-to-csv-string
  [res-ws]
  (let [num (:num res-ws)
        id (:id res-ws)
        datetime (:datetime res-ws)
        target (:target res-ws)
        words (:content res-ws)
        words-str (str/join ";" words)]
    (str/join "," [num id datetime target words-str])
    ))

