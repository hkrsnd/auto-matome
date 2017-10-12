(ns auto-matome.learn
  (:import [org.apache.commons.io FileUtils]
           [org.apache.commons.csv CSVRecord]
           [org.datavec.api.records.reader.impl.csv CSVRecordReader]
           [org.datavec.api.split FileSplit]
           [org.deeplearning4j.datasets.datavec RecordReaderDataSetIterator]
           [org.deeplearning4j.datasets.iterator.impl IrisDataSetIterator]
           [org.deeplearning4j.eval Evaluation]
           [org.deeplearning4j.nn.api OptimizationAlgorithm]
           [org.deeplearning4j.nn.conf MultiLayerConfiguration Updater NeuralNetConfiguration NeuralNetConfiguration$Builder]
           [org.deeplearning4j.nn.conf.layers OutputLayer OutputLayer$Builder RBM RBM$Builder RBM$HiddenUnit RBM$VisibleUnit DenseLayer DenseLayer$Builder]
           [org.deeplearning4j.nn.multilayer MultiLayerNetwork]
           [org.deeplearning4j.nn.params DefaultParamInitializer]
           [org.deeplearning4j.nn.weights WeightInit]
           [org.deeplearning4j.optimize.api IterationListener]
           [org.deeplearning4j.optimize.listeners ScoreIterationListener]
           [org.nd4j.linalg.api.ndarray INDArray]
           [org.nd4j.linalg.dataset DataSet]
           [org.nd4j.linalg.dataset SplitTestAndTrain]
           [org.nd4j.linalg.factory Nd4j]
           [org.nd4j.linalg.lossfunctions LossFunctions LossFunctions$LossFunction]
           [java.io File]

           [java.nio.file Files]
           [java.nio.file Paths]
           [java.util Arrays]
           [java.util Random])
  )


(defn learn-main []

  ;; Load CSV
  (def record-reader (CSVRecordReader. 0 ","))
  ;;  (.initialize record-reader, (FileSplit. ,(File. "resource/normalized-train-data.csv")))
  (def input-split (FileSplit.  (File. "resource/normalized-train-data.csv")))
  (.initialize record-reader input-split)
  (def label-index 0)
  (def num-classes 2)
  (def batch-size 50)
  (def lstm-layer-size 50)
  (def epoch 1)

  (def input-num 40)
  (def output-num 2)

  (println "here1")
  (def iterator (RecordReaderDataSetIterator. record-reader batch-size label-index num-classes))
  (println "here2")  
  ;;; Build Model
  (def conf
    (-> (NeuralNetConfiguration$Builder.)
        (.optimizationAlgo OptimizationAlgorithm/CONJUGATE_GRADIENT)
        (.iterations 1)
        (.learningRate 0.001)
                                        ;               :rms-decay 0.95
        (.seed 111)
        (.regularization true)
                                        ;               (.l2 2e-1)
        (.list)
        (.layer 0
                (-> (DenseLayer$Builder.)
                    (.nIn input-num)
                    (.nOut 30)
                    (.build)))
        (.layer 1
                (-> (DenseLayer$Builder.)
                    (.nIn 30)
                    (.nOut 10)
                    (.build)))
        (.layer 2
                (-> (OutputLayer$Builder. LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)
                    (.activation "softmax")
                    (.nIn 10)
                    (.nOut output-num)
                    (.build)
                    ))
       ; (.backprop true)
       ; (.pretrain false)
        (.build)))
  (println "here3")


    ;;; Create model
  (def model (MultiLayerNetwork. conf))
  (.init model)
  
  ;; set listner
  (def listener-freq 1)
  (.setListeners model (list (ScoreIterationListener. listener-freq)))


  ;; train model
  (loop [e 0]
    (.fit model iterator)
    (if (<= e epoch)
      (do
        (.reset iterator)
        (recur (+ e 1)))
      )
    )

   ;;; Evaluate and Log
;  (def eval (Evaluation. output-num))
;  (def test-data (RecordReaderDataSetIterator. record-reader batch-size label-index num-classes))
;  (loop [i 0]
;      (if (.hasNext test-data)
;        (let [ds (.next test-data)
;              output (.output model (.getFeatureMatrix ds))]
;          (.eval eval (.getLabels ds) output)
;          (recur (+ i 1)))
;          ))
;  (println (.stats eval))
  )
