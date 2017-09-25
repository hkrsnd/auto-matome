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
        [auto-matome.morpho]
        [auto-matome.data])
  (:require [clojure.string :as str]))


(require '[auto-matome.scrape :as scr])
(require '[auto-matome.scrape-origin :as scro])
(require '[auto-matome.io :as io])

(def home-url "http://blog.livedoor.jp/dqnplus/")
(def page-num 10)
(def response-file-num 8)
(def contents-resource "resource/contents.txt")
(def all-contents-path "resource/all-contents.txt")
(def contents-resource-base "resource/contents")
(def original-thread-responses-base "resource/original-responses/original-thread-")
(def matome-thread-responses-base "resource/matome-responses/matome-thread-")
(def words-resource-path "resource/words.txt")
(def matome-thread-responses-with-words-resource-base "resource/matome-responses-with-words/matome-thread-")
(def original-thread-responses-with-words-resource-base "resource/original-responses-with-words/original-thread-")
(def ids-resource-path "resource/ids.txt")
(def original-urls-resource "resource/original-urls.txt")
(def matome-urls-resource "resource/matome-urls.txt")
(def original-and-matome-urls-resource "resource/original-and-matome-urls.txt")
(def vectors-resource-path "resource/vectors.txt")
(def dictionary-path "resource/dictionary.txt")
(def id-dictionary-path "resource/id-dictionary.txt")
(def original-thread-responses-csv-num 3073)

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
  (doall (pmap #(-> % scr/get-html-resource) urls)))
;; get matome thread urls until page-num page
(defn get-matome-thread-urls
  []
  (scr/get-thread-urls home-url page-num))
;; get original threads urls from matome-urls
(defn get-original-thread-urls
  [matome-urls]
  (let [matome-thread-srcs (par-get-html-resource matome-urls)
        original-thread-urls (doall (pmap #(-> % (scr/get-original-thread-url)) matome-thread-srcs))
        filterd (filter #(-> % scr/is-hayabusa-thread) original-thread-urls)]
    filterd
    ))
;; returns pair [original-url, matome-url]
(defn get-original-and-matome-urls
  [matome-urls]
  (let [matome-thread-srcs (par-get-html-resource matome-urls)
        original-thread-urls (doall (pmap #(-> % (scr/get-original-thread-url)) matome-thread-srcs))
        original-and-matome-urls (apply map vector  [original-thread-urls matome-urls])
        filterd (filter #(-> % first scr/is-hayabusa-thread) original-and-matome-urls)]
    filterd
    ))

(defn record-original-and-matome-urls
  [original-and-matome-urls]
  (io/record-original-and-matome-urls original-and-matome-urls original-and-matome-urls-resource))

(defn read-original-and-matome-urls
  []
  (io/read-original-and-matome-urls original-and-matome-urls-resource))

(defn get-responses-each-original-threads
  [original-urls]
  (let [original-srcs (par-get-html-resource original-urls)]
    (doall (map #(-> % scro/get-original-responses) original-srcs))))

(defn get-responses-each-matome-threads
  [matome-urls]
  (let [matome-srcs (par-get-html-resource matome-urls)]
    (pmap #(-> % scr/get-matome-responses) matome-srcs)))

(defn record-original-urls
  [original-urls]
  (io/write-strings-line original-urls original-urls-resource))

(defn read-original-urls
  []
  (io/read-contents original-urls-resource))

(defn record-matome-urls
  [matome-urls]
  (io/write-strings-line matome-urls matome-urls-resource))

(defn read-matome-urls
  []
  (io/read-contents matome-urls-resource))

(defn record-responses
  [responses file-path]
  (let [res-strs (map #(response-to-string %) responses)]
    (io/write-strings res-strs file-path)))

(defn record-contents
  [responses file-path]
  (let [contents (map #(:content %) responses)]
    (io/write-strings-line contents file-path)))

(defn record-original-and-matome-responses-list-to-indexed-file
  [original-responses-list matome-responses-list] ; this would be read from resource file
  (let [paired-responses-list (zipmap original-responses-list matome-responses-list)
        indexed-paired-responses-list (map-indexed #(vector %1 %2) paired-responses-list)]
    (doall (map (fn [index-and-paired-responses]
            (let [index (first index-and-paired-responses)
                  paired-responses (second index-and-paired-responses)
                  original-responses (first paired-responses)
                  matome-responses (second paired-responses)
                  original-record-path (str/join [original-thread-responses-base index ".csv"])
                  matome-record-path (str/join [matome-thread-responses-base index ".csv"])]
              (record-responses original-responses original-record-path)
              (record-responses matome-responses matome-record-path)              
              )) indexed-paired-responses-list))))

(defn read-responses-from-indexed-files
  []
  (let [indexes (range response-file-num)]
    (doall (map (fn [index]
                   (let [matome-responses (io/read-csv-responses (str/join [matome-thread-responses-base index ".csv"]))
                         original-responses (io/read-csv-responses (str/join [original-thread-responses-base index ".csv"]))]
                     [matome-responses original-responses]
                     ))
                   indexes))))

(defn to-responses-with-words
  [responses]
  (doall (pmap (fn [res] (to-response-with-words res)) responses)))

(defn record-responses-with-words
  [responses-with-words file-path]
  (let [res-strs (doall (pmap (fn [res] (response-with-words-to-csv-string res)) responses-with-words))]
    (io/write-strings-line res-strs file-path)))

(defn read-responses-with-words-from-indexed-files
  []
  (let [indexes (range response-file-num)]
    (doall (map (fn [index]
                   (let [matome-responses (io/read-responses-with-words (str/join [matome-thread-responses-with-words-resource-base index ".csv"]))
                         original-responses (io/read-responses-with-words (str/join [original-thread-responses-with-words-resource-base index ".csv"]))]
                     [matome-responses original-responses]
                     ))
                indexes))))

(defn read-all-original-responses
  []
  (let [indexes (range response-file-num)]
    (flatten (map (fn [index]
                    (io/read-csv-responses (str/join [original-thread-responses-base index ".csv"]))) indexes))))

(defn read-all-original-responses-with-words
  []
  (let [indexes (range response-file-num)]
    (flatten (map (fn [index]
                   (io/read-responses-with-words (str/join [original-thread-responses-with-words-resource-base index ".csv"])))
                indexes))))

(defn record-original-and-matome-responses-with-words
  [original-responses-list matome-responses-list]
  (let [paired-responses-list (zipmap original-responses-list matome-responses-list)
        indexed-paired-responses-list (map-indexed #(vector %1 %2) paired-responses-list)]
    (doall (map (fn [index-and-paired-responses]
            (let [index (first index-and-paired-responses)
                  paired-responses (second index-and-paired-responses)
                  original-responses (first paired-responses)
                  matome-responses (second paired-responses)
                  original-record-path (str/join [original-thread-responses-with-words-resource-base index ".csv"])
                  matome-record-path (str/join [matome-thread-responses-with-words-resource-base index ".csv"])]
              (record-responses-with-words original-responses original-record-path)
              (record-responses-with-words matome-responses matome-record-path)              
              )) indexed-paired-responses-list))))

(defn read-all-response-csv
  []
  (let [rs (range original-thread-responses-csv-num)]
    (flatten
     (doall (pmap #(let [csv-path (str/join [original-thread-responses-base % ".csv"])]
              (println (str/join ["reading: " csv-path]))
              (io/read-csv-responses csv-path)
              ) rs)))))

(defn make-words-set-from-contents-resource
  [file-path]
  (let [contents (io/read-contents file-path)
        analyzed (pmap #(-> % morphological-analysis-sentence) contents)
        words-set (set (flatten (map (fn [x] (map (fn [y] (first y)) x)) analyzed)))]
    words-set))

(defn make-words-set-from-indexed-files
  []
  (let [original-responses-with-words (read-all-original-responses-with-words)
        contents (flatten (map #(:content %) original-responses-with-words))
        analyzed (pmap #(-> % morphological-analysis-sentence) contents)
        words-set (set (flatten (map (fn [x] (map (fn [y] (first y)) x)) analyzed)))]
    words-set))

(defn record-words
  [words]
  (io/write-strings-line words words-resource-path))

(defn read-words
  []
  (io/read-contents words-resource-path))

(defn record-dictionary
  [word-index-maps]
  (io/record-dictionary word-index-maps dictionary-path))

(defn read-dictionary
  []
  (io/read-dictionary dictionary-path))

(defn record-ids
  [responses]
  (let [ids (map #(:id %) responses)]
    (io/write-strings-line ids ids-resource-path)))

(defn read-ids
  []
  (io/read-contents ids-resource-path))

(defn read-id-dictionary
  []
  (io/read-id-dictionary id-dictionary-path))

(defn record-id-dictionary
  [id-index-map]
  (io/record-id-dictionary id-index-map id-dictionary-path))

(defn record-vectors
  [vecs]
  (let [csv-strs (doall (pmap #(vector-to-csv-string %) vecs))]
    (io/write-strings-line csv-strs vectors-resource-path)))

(defn test01
  []
  (let [matome-thread-urls (get-matome-thread-urls)
        original-thread-urls (get-original-thread-urls matome-thread-urls)]
    (record-original-urls original-thread-urls)
    ))

(defn test011
  []
  (let [matome-thread-urls (get-matome-thread-urls)
        original-and-matome-urls (get-original-and-matome-urls matome-thread-urls)]
    original-and-matome-urls))

;(defn test02
;  []
;  (let [original-urls (read-original-urls)
;        original-responses-list (get-responses-each-original-threads original-urls)
;        ;indexes (range 1 (count original-responses-list))
;        indexed-responses-list (map-indexed #(vector %1 %2) original-responses-list)
;        ;record-path (map #(str/join ["original-thread-" (str/str (first %))]) indexed-responses)
;        ]
;    (pmap (fn [index-and-responses]
;            (let[index (first index-and-responses)
;                 responses (second index-and-responses)
;                 num-of-responses (count responses)
;                 record-path (str/join [original-thread-responses-base index ".csv"])
;                 ]
;              (println record-path)
;              (record-responses responses record-path)
;              )) indexed-responses-list)
;    )
;  )

;(defn test03
;  []
;  (let [original-urls (read-original-urls)
;        original-responses-list (get-responses-each-original-threads original-urls)
;        ]
;;    (println original-responses-list)
;    (record-responses-list-to-indexed-file original-responses-list 'original)))

(defn test04
  []
  (let [matome-src (scr/get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1940167.html")]
    (scr/get-matome-responses matome-src)
    ))

(defn test05
  []
  (let [all-responses (read-all-response-csv)]
    (record-contents all-responses all-contents-path)
    ))

(defn test06
  []
  (let [words (make-words-set-from-indexed-files)]
    (record-words words)
    ))

(defn test07
  []
  (let [words (read-words)
        dictionary (from-set-to-dictionary words)]
    (record-dictionary dictionary)
    ))

;;record ids
(defn test08
  []
  (let [responses (read-all-original-responses)]
    (record-ids responses)
    ))

;; record id dictionary
(defn test09
  []
  (let [ids (read-ids)
        dictionary (from-set-to-id-dictionary ids)]
    (record-id-dictionary dictionary)
    )
  )

(defn test10
  []
  (let [responses (read-all-original-responses)
        dic (read-dictionary)
        id-dic (read-id-dictionary)
        vecs (doall (pmap #(response-to-vector % dic id-dic) responses))
        padded (padding-vectors vecs)]
    padded
    ))

(defn test11
  []
  (let [responses (read-all-response-csv)]
    (to-responses-with-words responses)))

(defn test12
  []
  (let [responses (read-responses-from-indexed-files)
        original-responses-list (doall (map #(first %) responses))
        matome-responses-list (doall (map #(second %) responses))
        original-responses-with-words (doall (map #(to-responses-with-words %) original-responses-list))
        matome-responses-with-words (doall (map #(to-responses-with-words %) matome-responses-list))]
    (record-original-and-matome-responses-with-words original-responses-with-words matome-responses-with-words)))

;(defn test13
;  []
;  (let [
;        dic (read-dictionary)
;        id-dic (read-id-dictionary)
;        responses-with-words (read-responses-with-words)]
;    (doall (pmap #(response-with-words-to-vector % dic id-dic) responses-with-words))
;    ))

;(defn test14
;  []
;  (let [dic (read-dictionary)
;        id-dic (read-id-dictionary)
;        responses-with-words (read-responses-with-words)
;        vecs (doall (pmap #(response-with-words-to-vector % dic id-dic) responses-with-words))
;        padded (padding-vectors vecs)]
;    (record-vectors padded)
;    ))

;(defn test15
;  []
;  (let [matome-urls (get-matome-thread-urls)
;        matome-responses (get-responses-each-matome-threads matome-urls)]
;    (doall (map #(println %) matome-urls))
;    (record-responses-list-to-indexed-file matome-responses 'matome)
;    ))
;
(defn test16
  []
  (let [;matome-urls (get-matome-thread-urls)
                                        ;original-and-matome-urls (get-original-and-matome-urls matome-urls)
        original-and-matome-urls (read-original-and-matome-urls)
       ; original-and-matome-urls [["http://hayabusa3.2ch.sc/test/read.cgi/news/1505689369/"]["http://blog.livedoor.jp/dqnplus/archives/1940297.html"]]
        original-urls (doall (map #(first %) original-and-matome-urls))
        matome-urls (doall (map #(second %) original-and-matome-urls))
        original-responses-list (get-responses-each-original-threads original-urls)
        matome-responses-list (get-responses-each-matome-threads matome-urls)]
;    (println matome-urls)
;    (println original-urls)
;    (println (first original-responses-list))
                                        ;(println (first matome-responses-list))
    (println original-and-matome-urls)
    (record-original-and-matome-responses-list-to-indexed-file original-responses-list matome-responses-list)
    ))

(defn test17
  []
;  (get-responses-each-matome-threads ["http://blog.livedoor.jp/dqnplus/archives/1940297.html"])
  (get-responses-each-original-threads ["http://hayabusa3.2ch.sc/test/read.cgi/news/1505689369/"])
  )

(defn test18
  []
  (let [matome-urls (read-matome-urls)
        original-and-matome-urls (get-original-and-matome-urls matome-urls)]
    (io/write-strings-line (doall (map #(str/join "," %) original-and-matome-urls)) original-and-matome-urls-resource)
    ))

(defn test19
  []
  (let [matome-urls (get-matome-thread-urls)]
    (record-matome-urls matome-urls)))

(defn test20
  []
  (read-responses-from-indexed-files))

(defn test21
  []
  (make-words-set-from-indexed-files))

(defn -main
  [& args]
  (println "main"))
