(ns match-block.test-util
  (:use [midje.sweet]))

(defmacro facts-about [x & rest]
  `(facts ~(str
             "about "
             (cond (symbol? x) (str "`" x "`")
                   :else x))
          ~@rest))
