(ns auto-matome.core
  (:gen-class)
  (:import (org.apache.lucene.analysis Analyzer TokenStream)
           (org.apache.lucene.analysis.ja JapaneseAnalyzer)
           (org.apache.lucene.analysis.ja.tokenattributes BaseFormAttribute InflectionAttribute PartOfSpeechAttribute ReadingAttribute)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)
           (org.apache.lucene.util Version))
  (:use (incanter core stats charts io))
  (:require [clojure.string :as str]))


(def version (Version/LUCENE_44))
(require '[auto-matome.scrape :as scr])
(require '[auto-matome.scrape-origin :as scro])
(def home-url "http://blog.livedoor.jp/dqnplus/")
(def page-num 3)

(defn get-responses
  []
  (let [origin-urls (flatten (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num)))]
    origin-urls
    (doall (map #(-> % scr/get-html-resource scro/get-responses) origin-urls))
    )
  )

(defn -main
  [& args]
  (get-responses)
  )

;  (println "===== Simple Pattern =====")
;  (doseq [t (morphological-analysis-sentence "黒い大きな瞳の男の娘")]
;    (println t))
;
;  (println "===== Filter Pattern =====")
                                        ;  (doseq [t (morphological-analysis-sentence

;             "僕はウナギだし象は鼻が長い"
;             #(not (nil? (re-find #"名詞" (nth % 2)))))]
;    (println t)))

;;  (println "===== 坊ちゃん =====")
;;  (let [tokens (morphological-analysis-sentence (slurp "bocchan.txt")
;;                                                #(not (nil? (re-find #"名詞" (nth % 2)))))
;;        words (flatten (map #(first %) tokens))]
;;    (view (bar-chart (keys a(top10 words)) (vals (top10 words))))
;;    (save (bar-chart (keys (top10 words)) (vals (top10 words))) "natume.png" :width 600)
;;    (save (bar-chart (keys (top100 words)) (vals (top100 words))) "natume_zip.png" :width 600)))

