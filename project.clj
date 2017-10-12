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
                 [org.clojure/clojure "1.7.0"]
                 [org.deeplearning4j/deeplearning4j-core "0.6.0"]
                 [org.deeplearning4j/deeplearning4j-ui "0.6.0"]
                 [org.deeplearning4j/deeplearning4j-nlp "0.6.0"]
                 [org.apache.commons/commons-io "1.3.2"]
                 [org.apache.commons/commons-csv "1.5"]
                 [org.datavec/datavec-api "0.6.0"]
                 [org.nd4j/nd4j-native "0.6.0"]
                 ;[org.nd4j/canova-api "0.0.0.4"]
                 [org.clojure/data.json "0.2.6"]
                 ]
  :main auto-matome.core
  )
