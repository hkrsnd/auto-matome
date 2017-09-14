(ns auto-matome.morpho
  (:gen-class)
  (:import (org.apache.lucene.analysis Analyzer TokenStream)
           (org.apache.lucene.analysis.ja JapaneseAnalyzer)
           (org.apache.lucene.analysis.ja.tokenattributes BaseFormAttribute InflectionAttribute PartOfSpeechAttribute ReadingAttribute)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)
           (org.apache.lucene.util Version))
  (:use (incanter core stats charts io))
  (:require [clojure.string :as str]))

(def version (Version/LUCENE_44))

(defn morphological-analysis-sentence [^String sentence & predicates]
  (let [^Analyzer analyzer (JapaneseAnalyzer. version)]
    (with-open [^TokenStream token-stream (.tokenStream analyzer "" sentence)]
      (.reset token-stream)
      (let [^CharTermAttribute char-term (.addAttribute token-stream CharTermAttribute)
            ^BaseFormAttribute base-form (.addAttribute token-stream BaseFormAttribute)
            ^InflectionAttribute inflection (.addAttribute token-stream InflectionAttribute)
            ^PartOfSpeechAttribute part-of-speech (.addAttribute token-stream PartOfSpeechAttribute)
            ^ReadingAttribute reading (.addAttribute token-stream ReadingAttribute)]
        (loop [results []]
          (if (.incrementToken token-stream)
            (let [attrs [(.toString char-term)
                         (.getReading reading)
                         (.getPartOfSpeech part-of-speech)
                         (.getBaseForm base-form)
                         (.getInflectionType inflection)
                         (.getInflectionForm inflection)]]
              (if (or (empty? predicates)
                      (reduce (fn [b p] (and b (p attrs)))
                              true
                              predicates))
                (recur (conj results attrs))
                (recur results)))
            (do (.end token-stream)
                results)))))))

(defn word-count [words]
 (reduce (fn [words word] (assoc words word (inc (get words word 0))))
  {}
  words))

(defn wc-result [words]
  (reverse (sort-by second (word-count words))))
(defn top10 [words]
  (take 10 (wc-result words)))
(defn top100 [words]
  (take 100 (wc-result words)))
