(ns auto-matome.core
  (:gen-class)
  (:import (org.apache.lucene.analysis Analyzer TokenStream)
           (org.apache.lucene.analysis.ja JapaneseAnalyzer)
           (org.apache.lucene.analysis.ja.tokenattributes BaseFormAttribute InflectionAttribute PartOfSpeechAttribute ReadingAttribute)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)
           (org.apache.lucene.util Version))
  (:use (incanter core stats charts io)
        [auto-matome.thread]
        [auto-matome.morpho])
  (:require [clojure.string :as str]))


(require '[auto-matome.scrape :as scr])
(require '[auto-matome.scrape-origin :as scro])
(require '[auto-matome.io :as io])

(def home-url "http://blog.livedoor.jp/dqnplus/")
(def page-num 1)

(defn get-responses
  []
  (let [origin-urls (flatten (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num)))]
    origin-urls
    (flatten
     (doall (map #(-> % scr/get-html-resource scro/get-responses) origin-urls)))
    )
  )

(defn -main
  [& args]
  (get-responses)
  )

(defn test01
  []
  (let [origin-url "http://hayabusa3.2ch.sc/test/read.cgi/news/1505522180/"
        responses (-> origin-url scr/get-html-resource scro/get-responses)
        contents (map #(-> % :content) responses)]
    (io/write-strings contents)
    )
  )

(defn test02
  []
;  (doseq [x (io/read-contents "resource/contents.txt")]
  (map #(-> % morphological-analysis-sentence) (io/read-contents "resource/contents.txt"))
  )

(defn test03
  []
  (let [responses (get-responses)
        contents (map #(-> % :content) responses)]
    (io/write-strings contents)
    ))


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

