(defproject auto-matome "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter "1.5.1"]
                 [org.apache.lucene/lucene-analyzers-kuromoji "4.4.0"]
                 [net.sourceforge.htmlcleaner/htmlcleaner "2.2"]
                 [pjstadig/utf8 "0.1.0"]
                 [com.github.kyleburton/clj-xpath "1.4.1"]
;                 [clj-tagsoup/clj-tagsoup "0.3.0"][enlive "1.1.5"]
                 [enlive "1.1.5"]
                 [reaver "0.1.2"]
                 [org.clojure/data.zip "0.1.2"]
                 ]
  :main auto-matome.core
  )
