(ns auto-matome.scrape
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [pjstadig.utf8 :as utf8]
            [clj-xpath.core :as xpath]
            [net.cgrand.enlive-html :as en]
            )
                                        ;[pl.danieljanus.tagsoup :as soup]
                                        ;[clojure.contrib.zip-filter :as z]
                                        ;[reaver]
                                        ;[clojure.zip-filter :as z]
                                        ;[clojure.zip-filter.xml :as zf]
  (:use [clojure.data.zip.xml]
        [clojure.java.io]
        )
  (:import [org.htmlcleaner HtmlCleaner CompactXmlSerializer]))

(defstruct response :num :name :datetime :id :content)
(defstruct thread :url :responses)

(defn get-html-resource
  [url]
  (en/html-resource (reader url :encoding "JISAutoDetect")))

(defn get-original-thread-url
  [html-src]
  (let [re #"http.*"
        moto-url (second
                  (:content
                   (first
                    (en/select html-src
                               [:.mainmore :.aa :span]))))]
    (re-find re moto-url)))

(defn parse-response-num
  [str-num] ; "n: "
  (let [re #"\d+"]
    (re-find re str-num)))

(defn parse-date-id
  [date-id-str] ; " 2017/09/04(月) 08:58:42.97 ID:8AaMbvih0"
  (let [re-date #"\d+\/\d+\/\d+"
        re-time #"\d+:\d+:\d+\.\d+"
        re-id #"(ID:)(.+)"
        date (re-find re-date date-id-str)
        time (re-find re-time date-id-str)
        id (last (re-find re-id date-id-str))
        ]
    {:datetime (str/join [date "-" time])
     :id id}
    ))

(defn get-matome-responses
  [matome-src]
  (let [num-name-date-ids (map #(:content %) (en/select matome-src [:.mainmore :.t_h]))
        nums (map #(-> % first parse-response-num) num-name-date-ids)
        names (map #(-> % second :content first) num-name-date-ids)
        date-id-strs (map #(-> % last :content first) num-name-date-ids )
        datetimes (map #(-> % parse-date-id :datetime) date-id-strs)
        ids (map #(-> % parse-date-id :id) date-id-strs)
        contents (filter #(string? %)
                         (map #(-> % :content first) (en/select matome-src [:.mainmore :.t_b])))
        zipped (apply map list [nums names datetimes ids contents])
        ]
;    (println contents)
;    (println zipped)
    (map #(struct response
                  (nth % 0)
                  (nth % 1)
                  (nth % 2)
                  (nth % 3)
                  (nth % 4)
                  ) zipped)
    
    ))

;; get each threds's urls from home url and number of pages
(defn get-thread-urls
  [home-url n]
  (defn get-thread-urls-loop
    [home-url n count]
    (if (> count n)
      []
      (let [url (if (= count 1)
                  home-url
                  (str/join [home-url "?p=" (str count)])
                  )
            src (get-html-resource url)
            tmp1  (map #(-> % :content first) (en/select src [:.titlebody]))
            tmp2 (flatten (map #(-> % :content first) tmp1))
            thread-urls (map #(-> % :attrs :href) tmp2)]
        (cons thread-urls (get-thread-urls-loop home-url n (+ count 1)))
        )))
  (flatten (get-thread-urls-loop home-url n 1))
  )


(defn test02
  [url]
  (let [
        thread-urls (get-thread-urls url 2)
        srcs (map #(get-html-resource %) thread-urls)
        ]
                                        ;   (get-original-thread-url src)
    ;(doall (map #(println %) thread-urls))
;   (map #(get-html-resource %) thread-urls)
;    (map (fn [x]
 ;         (println x)
  ;        ))
  ; (get-matome-responses (get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1938938.html"))
   ;(get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1938938.html")
    (doall (map #(get-matome-responses %) srcs))
;;    thread-urls
    )
  )

;(defn html->node
;  [cleaner html-src]
;  (doto (.getProperties cleaner)
;    (.setOmitComments true)
;    (.setPruneTags "sctipt,style")
;    (.setOmitXmlDeclaration true)
;    (.setRecognizeUnicodeChars true)
;    )
;  (.clean cleaner html-src ))
;
;(defn node->xml
;  [cleaner node]
;  (let [props (.getProperties cleaner)
;        xml-serializer (CompactXmlSerializer. props)]
;    (-> (.getAsString xml-serializer node)
;        java.io.StringReader.
;        org.xml.sax.InputSource.
;        xml/parse
;        )))

;(defn test01
;  [url]
;  (let [cleaner (HtmlCleaner.)
;        page-src (slurp url)
;        node (html->node cleaner page-src)
;        xml (node->xml cleaner node)
;        zip (zip/xml-zip xml)
;        ;ori-url (get-original-thread-url page-src)
;        ]
;;    (println zip)
;                                        ; (xml-> zip :html :body :div :div :div :div :div :div :div :div (attr= :class "mainmore"))
;     (prn (xpath/$x:tag* "//author" *some-xml*))
;;    (xml-> zip descendants  (attr= :class "aa"))

;     ))


;(defn test02
;  [url]f
;  (let [src (slurp url)
;        parsed (reaver/parse src)
;        ]
;    (println (reaver/extract-from parsed ".itemlist .athing"
;                  [:span]
;                  ".span" reaver/text
;                  ))
;    )
;)

;(def x (test01 "http://himasoku.com/archives/52010524.html#more"))
