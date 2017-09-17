(ns auto-matome.regex
  )

(defn re-find-ex
  [re str]
  (if (string? str)
    (re-find re str)
    "")
  )
