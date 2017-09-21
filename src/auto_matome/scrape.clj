(ns auto-matome.scrape
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [pjstadig.utf8 :as utf8]
            [clj-xpath.core :as xpath]
            [net.cgrand.enlive-html :as en])
  (:use [clojure.data.zip.xml]
        [clojure.java.io]
        [auto-matome.thread]
        [auto-matome.regex])
  (:import [org.htmlcleaner HtmlCleaner CompactXmlSerializer]))

(defn get-html-resource
  [url]
  (try
    (en/html-resource (reader url :encoding "JISAutoDetect"))
    (catch Exception e nil)))

(defn get-original-thread-url
  [html-src]
  (let [re #"http.*"
        moto-url (second
                  (:content
                   (first
                    (en/select html-src
                               [:.mainmore :.aa :span]))))]
      (re-find-ex re moto-url)))

(defn parse-response-num
  [str-num] ; "n: "
  (let [re #"\d+"]
      (re-find-ex re str-num)))

(defn parse-date-id
  [date-id-str] ; " 2017/09/04(月) 08:58:42.97 ID:8AaMbvih0"
  (let [re-date #"\d+\/\d+\/\d+"
        re-time #"\d+:\d+:\d+\.\d+"
        re-id #"(ID:)(.+)"
        date (re-find-ex re-date date-id-str)
        time (re-find-ex re-time date-id-str)
        id (last (re-find-ex re-id date-id-str))
        ]
    {:datetime (str/join [date "-" time]) :id id}))

(defn nth-arg
  [i & args]
  (nth args i))

;(defn arg1 (partial nth-arg 1))
;; >>999 => 999
;; >>36 => 36
(defn parse-target
  [row-target]
  (let [re-target #"(>>)([0-9]+)"
        matched (re-find-ex re-target row-target)]
    (if (nil? matched)
      "nil"
      (nth matched 2))))

(defn parse-response
  [row-res]
  (let [ls (:content row-res)
        strs (filter #(and (string? %) (not= " " %)) ls)
        joined-tmp (str/join strs)
        joined (str/join [joined-tmp "\n"])]
                                        ;    (str/join (map #(-> % (filter string?) (str/join "\n"))))
    joined))

(defn get-matome-responses
  [matome-src]
    (let [num-name-date-ids (map #(:content %) (en/select matome-src [:.mainmore :.t_h]))
          nums (map #(-> % first parse-response-num) num-name-date-ids)
          names (map #(-> % second :content first) num-name-date-ids)
          date-id-strs (map #(-> % last :content first) num-name-date-ids )
          datetimes (map #(-> % parse-date-id :datetime) date-id-strs)
          ids (map #(-> % parse-date-id :id) date-id-strs)
          ;contents (filter #(string? %)
                                        ;                 (map #(-> % :content first) (en/select matome-src [:.mainmore :.t_b])))
          contents (map #(-> % parse-response) (en/select matome-src [:.mainmore :.t_b]))
          targets (map #(-> % :content second :content first  parse-target) (en/select matome-src [:.mainmore :.t_b]))
          zipped (apply map list [nums ids datetimes targets contents])
          ]
      (map #(struct response
                    (nth % 0)
                    (nth % 1)
                    (nth % 2)
                    (nth % 3)
                    (nth % 4)
                    ) zipped)))

(defn is-hayabusa-thread
  [url]
  (println url)
  (let [re-hayabusa #".*hayabusa3\.2ch\.sc.*"
        hayabusa-url (re-find-ex re-hayabusa url)]
    (if (= nil hayabusa-url)
      false
      true)))

;
;(defn select-hayabusa-thread-urls
;  [urls]
;      );
;  (let [;ori-urls (map #(-> % get-html-resource get-original-thread-url) urls)]
;    (filter #(is-hayabusa-thread %) ori-urls)
;    )
;  )

;; get each matome threds's urls from home url and number of pages
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
            thread-urls-tmp (-> src (en/select [:a.continues]) flatten)
            thread-urls (map #(-> % :attrs :href) thread-urls-tmp)]
            ;tmp1  (map #(-> % :content first) (en/select src [:.titlebody]))
            ;tmp2 (flatten (map #(-> % :content first) tmp1))
            ;thread-urls (map #(-> % :attrs :href) tmp2)]
        (cons thread-urls (get-thread-urls-loop home-url n (+ count 1)))
        )))
  (flatten
   (get-thread-urls-loop home-url n 1))
  )

                                        ;   (get-original-thread-url src)
    ;(doall (map #(println %) thread-urls))
;   (map #(get-html-resource %) thread-urls)
;    (map (fn [x]
 ;         (println x)
  ;        ))
  ; (get-matome-responses (get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1938938.html"))
   ;(get-html-resource "http://blog.livedoor.jp/dqnplus/archives/1938938.html")
    ;(doall (map #(get-matome-responses %) srcs))
;;    thread-urls


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
