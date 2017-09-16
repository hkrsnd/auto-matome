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
        )
  (:import [org.htmlcleaner HtmlCleaner CompactXmlSerializer]))

;; scrape an original thread
(defn get-html-resource
  [url]
  (scr/get-html-resource url))

(defn parse-response
  [row-res]
  (let [ls (:content row-res)
        strs (filter #(and (string? %) (not= " " %)) ls)
        joined (str/join strs)]
                                        ;    (str/join (map #(-> % (filter string?) (str/join "\n"))))
    joined
    )
  )

;; .net ありなしの場合がある
(defn parse-ids
  [strids]
  (let [re-id #"(.+)(.net)"
        re-net-removed #""]
    (map #(if (= nil (re-find re-id %))
            %
            (second (re-find re-id %))
            ) strids)
    )
  )

(defn join-dates-times
  [dates times]
  (let [zipped (apply map list [dates times])]
    (map #(str/join [(first %) "-" (second %)]) zipped)
    ))

(defn drop-nth
  [n coll]
  (->> coll
       (map vector (iterate inc 1))
       (remove #(zero? (mod (first %) n)))
       (map second)))

;; get a list of struct response corresponds to reponses in original thread
(defn get-responses
  [src]
  (let [contents (map #(parse-response %) (en/select src [:html :body :dl :dd]))
        dts (drop-nth 4 (filter #(string? %) (en/select src [:html :body :dl :dt text]))
                      )
        re-num #"(\d+)(\s：)"
        re-id #"(ID:)(.+)"
        re-date #"\d+\/\d+\/\d+"
        re-time #"\d+:\d+:\d+\.\d+"
        re-name #"(ID)()"
        nums (filter #(not= % nil) (map 
                                    #(second (re-find re-num %))
                                    dts))
        ;; TODO
        ids (parse-ids (filter #(not= % nil) (map 
                                   #(nth (re-find re-id %) 2)
                                   dts)))
        dates (filter #(not= % nil) (map 
                                     #(re-find re-date %)
                                     dts))
        times (filter #(not= % nil) (map 
                                     #(re-find re-time %)
                                     dts))
        date-times (join-dates-times dates times)
        zipped (apply map list [nums ids date-times contents])
        ]
    (map #(struct response
                  (nth % 0)
                  (nth % 1)
                  (nth % 2)
                  (nth % 3)
                  ) zipped)
    ))

(defn test01
  [url]
  (let [src (get-html-resource url)
        contents (get-responses src)]
    contents
    )
  )
