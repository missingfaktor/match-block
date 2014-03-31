(ns match-block.core
  (:import [clojure.lang IFn])
  (:use [match-block.ast-transformations :only [match-fn-block->defined-at?-block]]
        [clojure.core.match :only [match]]))

(defrecord MatchBlock [defined-at? fun]
  IFn
  (invoke [this a]
    ((:fun this) a))
  (invoke [this a b]
    ((:fun this) a b))
  (invoke [this a b c]
    ((:fun this) a b c))
  (invoke [this a b c d]
    ((:fun this) a b c d))
  (invoke [this a b c d e]
    ((:fun this) a b c d e))
  ; Do I have to repeat the above for every arity?
  (applyTo [this args]
    (apply (:fun this) args)))

(defn defined-at? [mblock & args]
  (apply (:defined-at? mblock) args))

(defn match-block? [obj]
  (instance? MatchBlock obj))

(defmacro match-block [args-vec & code]
  (let [match-fn-block `(fn ~args-vec
                          (match ~args-vec ~@code))]
    `(map->MatchBlock {:fun         ~match-fn-block
                       :defined-at? ~(match-fn-block->defined-at?-block match-fn-block)})))

(def empty-match-block
  (map->MatchBlock {:fun         (fn [& args]
                                   (throw (RuntimeException. "Empty match block.")))
                    :defined-at? (constantly false)}))
