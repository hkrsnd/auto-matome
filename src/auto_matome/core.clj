(ns auto-matome.core
  (:gen-class)
  (:import (org.apache.lucene.analysis Analyzer TokenStream)
           (org.apache.lucene.analysis.ja JapaneseAnalyzer)
           (org.apache.lucene.analysis.ja.tokenattributes BaseFormAttribute InflectionAttribute PartOfSpeechAttribute ReadingAttribute)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)
           (org.apache.lucene.util Version)
           [org.htmlcleaner HtmlCleaner CompactXmlSerializer])
  (:use (incanter core stats charts io)
        [auto-matome.thread]
        [auto-matome.morpho])
  (:require [clojure.string :as str]))


(require '[auto-matome.scrape :as scr])
(require '[auto-matome.scrape-origin :as scro])
(require '[auto-matome.io :as io])

(def home-url "http://blog.livedoor.jp/dqnplus/")
(def page-num 1)
(def contents-resource "resource/contents.txt")
(def contents-resource-base "resource/contents")
(def original-urls-path "resource/original-urls.txt")
(def dictionary-path "resource/dictionary.txt")
(def csv-num )

;(defn get-responses
;  []
;  (let [origin-urls (flatten (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num)))]
;    origin-urls
;    (flatten
;     (doall (map #(-> % scr/get-html-resource scro/get-responses) origin-urls)))
;    ))
;; pararell get-html-resource
(defn par-get-html-resource
  [urls]
  (pmap #(-> % scr/get-html-resource) urls))
;; get matome thread urls until page-num page
(defn get-matome-thread-urls
  []
  (scr/get-thread-urls home-url page-num)
  )
;; get original threads urls from matome-urls
(defn get-original-thread-urls
  [matome-urls]
  (let [matome-thread-srcs (par-get-html-resource matome-urls)
        original-thread-urls (pmap #(-> % (scr/get-original-thread-url)) matome-thread-srcs)
        filterd (filter #(-> % scr/is-hayabusa-thread) original-thread-urls)]
    filterd
    )
  )

(defn get-responses-each-original-threads
  [original-urls]
  (let [original-srcs (par-get-html-resource original-urls)]
    (pmap #(-> % scro/get-responses) original-srcs)
    )
  )

(defn get-responses-each-matome-threads
  [matome-urls]
  (let [matome-srcs (par-get-html-resource matome-urls)]
    (pmap #(-> % scr/get-matome-responses) matome-srcs)
    )
  )

(defn record-original-urls
  [original-urls]
  (io/write-strings-line original-urls original-urls-path))

(defn read-original-urls
  []
  (io/read-contents original-urls-path))

(defn record-responses
  [responses file-path]
  (let [res-strs (map #(response-to-string %) responses)]
    (io/write-strings res-strs file-path)
    )
  )

(defn record-responses-list-to-indexed-file
  [original-responses-list] ; this would be read from resource file
  (let [indexed-responses-list (map-indexed #(vector %1 %2) original-responses-list)]
    (pmap (fn [index-and-responses]
            (let[index (first index-and-responses)
                 responses (second index-and-responses)
                 num-of-responses (count responses)
                 record-path (str/join ["resource/original-thread-" index ".csv"])
                 ]
              (println record-path)
              (record-responses responses record-path)
              )) indexed-responses-list)
    )
  )

(defn test01
  []
  (let [matome-thread-urls (get-matome-thread-urls)
        original-thread-urls (get-original-thread-urls matome-thread-urls)]
    (record-original-urls original-thread-urls)
    ))

(defn test02
  []
  (let [original-urls (read-original-urls)
        original-responses-list (get-responses-each-original-threads original-urls)
        ;indexes (range 1 (count original-responses-list))
        indexed-responses-list (map-indexed #(vector %1 %2) original-responses-list)
        ;record-path (map #(str/join ["original-thread-" (str/str (first %))]) indexed-responses)
        ]
    (pmap (fn [index-and-responses]
            (let[index (first index-and-responses)
                 responses (second index-and-responses)
                 num-of-responses (count responses)
                 record-path (str/join ["resource/original-thread-" index ".csv"])
                 ]
              (println record-path)
              (record-responses responses record-path)
              )) indexed-responses-list)
    )
  )

(defn test03
  []
  (let [original-urls (read-original-urls)
        original-responses-list (get-responses-each-original-threads original-urls)
        ]
;    (println original-responses-list)
    (record-responses-list-to-indexed-file original-responses-list)
    )
  )

(defn test04
  []
  (let [matome-src (scr/get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1940167.html")]
    (scr/get-matome-responses matome-src)
    ))

;(defn record-original-urls
;  []
;  (let [origin-urls (flatten (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num)))]
;    (io/write-strings origin-urls origin-urls-path)
;  ))

;(defn get-responses-by-each-thread
;  []
;  (let [origin-urls (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num))]
;    (doall (map #(-> % scr/get-html-resource scro/get-responses) origin-urls))
;    ))

(defn make-words-set-from-text
  [file-path]
  (let [contents (io/read-contents file-path)
        analyzed (map #(-> % morphological-analysis-sentence) contents)
        words-set (set (flatten (map (fn [x] (map (fn [y] (first y)) x)) analyzed)))]
    words-set
  ))

(defn from-set-to-dictionary
  [words-set]
  (let [zipped (map-indexed #(vector %1 %2) words-set)]
    (loop [result {} tmp-zipped zipped]
      (let [zip (first tmp-zipped)]
        (if (empty? zip)
          result
          (recur (assoc result (second zip) (first zip)) (rest tmp-zipped)))))))

;  (let [size (count words-set)
;        range (range 1 size)
;        zipped (apply map list [words-set range])]
;;    (println zipped)
;    (loop [result {} zip (first zipped)]
;      (println zip)
;      (if (= zip nil)
;        result
;        (recur (assoc result (first zip) (second zip)) (rest zipped))
;        )
;      )
;    )
;  )

(defn -main
  [& args]
  (println "main"))

;(defn test01
;  []
;  (let [origin-url "http://hayabusa3.2ch.sc/test/read.cgi/news/1505522180/"
;        responses (-> origin-url scr/get-html-resource scro/get-responses)
;        contents (map #(-> % :content) responses)]
;    (io/write-strings contents contents-resource)
;    )
;  )
;
;(defn test02
;  []
;;  (doseq [x (io/read-contents "resource/contents.txt")]
;  (map #(-> % morphological-analysis-sentence) (io/read-contents "resource/contents.txt"))
;  )
;
;(defn test03
;  []
;  (let [responses (get-responses)
;        contents (map #(-> % :content) responses)]
;    (io/write-strings contents contents-resource)
;    ))
;
;(defn test04
;  []
;  (make-words-set-from-text contents-resource))
;
;(defn test05
;  []
;  (let [responses (get-responses)
;        contents (map #(-> % :content) responses)
;        buffer (io/write-strings contents contents-resource)
;        words (make-words-set-from-text contents-resource)]
;  (io/write-words words dictionary-path)
;  ))
;
;(defn test06
;  []
;  (let [;contents (io/read-contents contents-resource)
;        words (make-words-set-from-text contents-resource)]
;    (io/write-words words dictionary-path)
;    ))
;
;(defn test07
;  []
;  (let [words (make-words-set-from-text contents-resource)]
;    (from-set-to-dictionary words)
;    ))
;
;(defn test08
;  []
;  (let [responses-list (get-responses-by-each-thread)]
;    (loop [responses-list-tmp responses-list count 1]
;      (let [responses (first responses-list-tmp)]
;        (if (empty? responses)
;          (println "finished")
;          (let [contents (map #(-> % :content) responses)
;                contents-resource-path (str/join [contents-resource-base "_" count ".txt"])]
;            (io/write-strings contents contents-resource-path)
;            (recur (rest responses-list-tmp) (inc count))
;            )
;          ))
;      )
;    )
;  )
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

