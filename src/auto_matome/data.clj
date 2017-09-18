(ns auto-matome.data
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
    (map #({:word (first %) :index (second %)}) zipped)
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
    (map #(first %) analyzed)
    ))

(defn words-to-vector
  [words dictionary]
  (map (fn [word]
          (search-dictionary-by-word word dictionary)
          ) words))

(defn response-to-vector
  [response]
  ;;todo
  )

(defn datetime-to-vector
  [datetime]
  (let [re-datetime #"([0-9]+)/([0-9]+)/([0-9]+)-([0-9]+):([0-9]+):([0-9]+)\.[0-9]+"
        fined (re-find-ex re-datetime datetime)]
    (rest fined)
    )
  )

(defn id-to-vector
  [id dictionary]
  (search-dictionary-by-id id dictionary)
  )
