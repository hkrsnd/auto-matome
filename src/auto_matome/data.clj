(ns auto-matome.data
  (:use [auto-matome.thread]
        [auto-matome.morpho]))


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

(defn search-dictionary-by-word
  [word dictionary]
  (:index
   (first
    (filter #(= word (:word %)) dictionary))))

(defn text-to-words
  [text]
  (let [analyzed (morphological-analysis-sentence text)]
    (map #(first %) analyzed)
    ))

(defn words-to-num
  [words dictionary]
  (map (fn [word]
          (search-dictionary-by-word word dictionary)
          ) words))

