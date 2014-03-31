(ns match-block.combinators
  (:use [match-block.core]))

(defn fn->match-block [fun]
  (if (match-block? fun)
    fun
    (map->MatchBlock {:fun         fun
                      :defined-at? (constantly true)})))

(defn fallback-to-nil [mblock]
  (fn [& args]
    (if (apply defined-at? mblock args)
      (apply mblock args)
      nil)))

(defn invoke-with-fallback-fn [mblock fallback-fn & args]
  (let [fn-to-invoke (if (apply defined-at? mblock args)
                       mblock
                       fallback-fn)]
    (apply fn-to-invoke args)))
