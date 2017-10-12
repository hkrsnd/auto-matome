(ns auto-matome.data
  (:require [clojure.string :as str]
            [auto-matome.io :as io])
  (:use [auto-matome.thread]
        [auto-matome.morpho]
        [auto-matome.regex]))

(def limit-num 40)

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
    (map (fn [z] {:word (first z) :index (second z)}) zipped)))

(defn from-set-to-id-dictionary
  [id-set]
  (let [zipped (map-indexed #(vector %2 %1) id-set)]
    (map (fn [z] {:id (first z) :index (second z)}) zipped)))

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
    (doall (pmap #(first %) analyzed))))

(defn words-to-vector
  [words dictionary]
    (doall (pmap (fn [word]
                   (search-dictionary-by-word word dictionary)) words)))

(defn num-to-vector
  [num]
  [num])

(defn datetime-to-vector
  [datetime]
  (let [re-datetime #"([0-9]+)/([0-9]+)/([0-9]+)-([0-9]+):([0-9]+):([0-9]+)\.[0-9]+"
        fined (re-find-ex re-datetime datetime)]
    (rest fined)))

(defn id-to-vector
  [id dictionary]
  [(search-dictionary-by-id id dictionary)])

(defn target-to-vector
  [target]
  (if (= target "nil")
    ["0"]
    [target]))

; "10"->10, "09"->9
(defn parse-int [s]
  (try
    (if (integer? s)
      s
      (Integer. (re-find  #"\d+" s)))
    (catch Exception e 0)))

(defn response-to-vector
  [response dictionary id-dictionary]
  ;;todo
  (let [num-vec (num-to-vector (:num response))
        id-vec (id-to-vector (:id response) id-dictionary)
        datetime-vec (datetime-to-vector (:datetime response))
        target-vec (target-to-vector (:target response))
        words (text-to-words (:content response))
        content-vec (words-to-vector words dictionary)
        vec (doall (pmap #(parse-int %) (flatten [num-vec id-vec datetime-vec target-vec content-vec])))
        ]
    (print "ToVector: ")
    (print response)
    (println vec)
    vec
    ))

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
           (flatten [num-vec id-vec datetime-vec target-vec content-vec])))))

(defn to-response-with-words
  [response]
  (assoc response :content (text-to-words (:content response))))

(defn response-with-words-to-csv-string
  [res-ws]
  (let [num (:num res-ws)
        id (:id res-ws)
        datetime (:datetime res-ws)
        target (:target res-ws)
        words (:content res-ws)
        words-str (str/join ";" words)]
    (str/join "," [num id datetime target words-str])))

(defn vector-to-csv-string
  [vec]
  (let [str-vec (doall (pmap #(str %) vec))]
    (str/join "," str-vec)))

(defn find-max-length
  [vecs]
  (loop [max-length 0 vecs-tmp vecs]
        (if (empty? vecs-tmp)
          max-length
          (if (> (count (first vecs-tmp)) max-length)
            (recur (count (first vecs-tmp)) (rest vecs-tmp))
            (recur max-length (rest vecs-tmp))))))

(defn padding-zero
  [vec padding-num]
    (concat vec (repeat padding-num 0))
  )
;  (let [max-length (find-max-length vecs)]
;    (pmap (fn [vec]
;            (let [length (count vec)
;                  diff (- max-length length)
;                  add-part (repeat diff 0)]
;              (concat vec add-part)))
;          vecs)))

(defn padding-vectors
  [vecs]
  (doall (pmap (fn [vec] 
          (if (> (count vec) limit-num)
            (take limit-num vec)
            (padding-zero vec (- limit-num (count vec)))))
        vecs)))

(defn seq-or
  [bools]
  (loop [bool (first bools) tmp-bools (rest bools)]
    (if (empty? tmp-bools)
      false
      (if bool
        true
        (recur (first tmp-bools) (rest tmp-bools))))))

(defn selected?
  [original-response matome-responses]
  (seq-or (map (fn [matome-response] (= (:num original-response) (:num matome-response)))
            matome-responses)))

(defn generate-response-labels
  [original-responses-list matome-responses-list]
  (let [zipped-original-matome-responses-list (zipmap original-responses-list matome-responses-list)]
    (doall (map (fn [zip]
                  (let [original-responses (first zip)
                        matome-responses (second zip)]
                    (map (fn [o-res] (if (selected? o-res matome-responses)
                                       1
                                       0
                                       )) original-responses)
                    ))
                zipped-original-matome-responses-list))))


(defn to-int-vec
  [string-vec]
  (doall (mapv #(parse-int %) string-vec)))

(defn normalize
  [string-vecs max-word-index max-id-index]
  (let [int-vecs (doall (mapv
                     (fn [svec] (to-int-vec svec))
                     string-vecs))]
    
    (doall (map (fn [vec] (let [label  (nth vec 0)
                                num    (nth vec 1)
                                id     (nth vec 2)
                                year   (nth vec 3)
                                month  (nth vec 4)
                                day    (nth vec 5)
                                hour   (nth vec 6)
                                min    (nth vec 7)
                                sec    (nth vec 8)
                                target (nth vec 9)
                                words  (subvec vec 10)
                                ]
                            
                            (flatten 
                             [label
                              (double (/ num 1000))
                              (double (/ id max-id-index))
                              (double (/ year 2017))
                              (double (/ month 12))
                              (double (/ day 31))
                              (double (/ hour 24))
                              (double (/ min 60))
                              (double (/ sec 60))
                              (double (/ target 1000))
                              (mapv #(double (/ % max-word-index)) words)
                              ]
                             ))) int-vecs))
    ))
