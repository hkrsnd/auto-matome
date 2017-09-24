(ns auto-matome.scrape-origin
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [pjstadig.utf8 :as utf8]
            [clj-xpath.core :as xpath]
            [net.cgrand.enlive-html :as en]
            [auto-matome.scrape :as scr]
            )
  (:use [clojure.data.zip.xml]
        [clojure.java.io]
        [auto-matome.thread]
        [auto-matome.regex]
        )
  (:import [org.htmlcleaner HtmlCleaner CompactXmlSerializer]))

;; scrape an original thread
(defn get-html-resource
  [url]
  (scr/get-html-resource url))

;; .net ありなしの場合がある
;(defn parse-ids
;  [strids]
;  (let [re-id #"(.+)(.net)"
;        re-net-removed #""]
;    (map #(if (= nil (re-find-ex re-id %))
;            %
;            (second (re-find-ex re-id %))
;            ) strids)
;    )
;  )
;; >>999 => 999
(defn parse-target
  [row-target]
  (let [re-target #"(>>)([0-9]+)"
        matched (re-find-ex re-target row-target)]
    (if (nil? matched)
      "nil"
      (nth matched 2))
    )
  )


(defn drop-nth
  [n coll]
  (->> coll
       (map vector (iterate inc 1))
       (remove #(zero? (mod (first %) n)))
       (map second)))

(defn nth-arg [i & args]
  (nth args i))

(defn get-target-part
  [ls]
  (let [ls-tmp (map #(-> % :content first) ls)
        filtered (first (filter #(not (nil? %)) ls-tmp))]
    filtered
    ))

(defn parse-num
  [up-line]
  (re-find-ex #"\d+" (first up-line)))

(defn join-dates-times
  [dates times]
  (let [zipped (apply map list [dates times])]
    (map #(str/join [(first %) "-" (second %)]) zipped)
    ))

(defn join-date-time
  [date time]
  (if (or (nil? date)  (nil? time))
    nil
    (str/join [date "-" time])))

(defn parse-datetime
  [date-id-string]
  (let [date (re-find-ex #"\d+\/\d+\/\d+" date-id-string)
        time (re-find-ex #"\d+:\d+:\d+\.\d+" date-id-string)]
        (join-date-time date time)))

(defn parse-id
  [date-id-string]
  (second (re-find-ex #"ID:(.+)" date-id-string)))

(defn parse-target
  [down-block]
  (let [row-target (-> down-block second :content first)
        re-target #">>([0-9]+)"
        matched (re-find-ex re-target row-target)]
    (if (nil? matched)
      "nil"
      (second matched))))

(defn parse-response
  [row-res]
  (let [ls (:content row-res)
        strs (filter #(and (string? %) (not= " " %)) ls)
        joined (str/join strs)]
                                        ;    (str/join (map #(-> % (filter string?) (str/join "\n"))))
    joined
    )
  )
(defn parse-contents
  [down-block]
  (let [strings (filter string? down-block)
        string (str/join  strings)]
    string))

  
(defn get-original-responses
  [src]
  (let [
        ;(94 ： {:tag :font, :attrs {:color green}, :content ({:tag :b, :attrs nil, :content (名無しさん＠涙目です。)})} ：2017/09/18(月) 08:29:33.50 ID:D9FF6Umo0.net)
        up-lines (doall (map #(-> % :content) (en/select src [:html :body :dl :dt])))
        down-blocks (doall (map #(-> % :content) (en/select src [:html :body :dl :dd])))
        nums (doall (map #(parse-num %) up-lines))
        ;"：2017/09/18(月) 08:03:17.41 ID:9+6bHTs50.net"
        date-id-strings (doall (map #(nth % 2) up-lines))
        date-times (doall (map #(parse-datetime %) date-id-strings))
        ids (map #(parse-id %) date-id-strings)
        targets (flatten (map #(parse-target %) down-blocks))
        contents  (doall (map (fn[b] (parse-contents b)) down-blocks))
        zipped (apply map list [nums ids date-times targets contents])
        ]
    (map #(struct response
                  (nth % 0)
                  (nth % 1)
                  (nth % 2)
                  (nth % 3)
                  (nth % 4)
                  ) zipped)
    )
  )

