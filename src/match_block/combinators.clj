(ns match-block.combinators
  (:use [match-block.core])
  (:import [match_block.core PartialFunction]))

(defn fn->partial-fn [fun]
  (if (instance? PartialFunction fun)
    fun
    (map->PartialFunction {:fun fun
                           :in-domain? (constantly true)})))

(defn fallback-to-nil [pfun]
  (fn [& args]
    (if (apply in-domain? pfun args)
      (apply pfun args)
      nil)))

(defn invoke-with-fallback-fn [pfun fallback-fn & args]
  (let [fn-to-invoke (if (apply in-domain? pfun args)
                       pfun
                       fallback-fn)]
    (apply fn-to-invoke args)))

