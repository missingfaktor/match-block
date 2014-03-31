(ns match-block.core
  (:import [clojure.lang IFn])
  (:use [match-block.ast-transformations :only [match-fn-block->in-domain?-block]]
        [clojure.core.match :only [match]]))

(defrecord PartialFunction [in-domain? fun]
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

(defn in-domain? [pfun & args]
  (apply (:in-domain? pfun) args))

(defmacro partial-fn [args-vec & code]
  (let [match-fn-block `(fn ~args-vec
                          (match ~args-vec ~@code))]
    `(map->PartialFunction {:fun        ~match-fn-block
                            :in-domain? ~(match-fn-block->in-domain?-block match-fn-block)})))

(defmacro define-partial-fn [var-name & rest]
  `(def ~var-name (partial-fn ~@rest)))

(def empty-partial-fn
  (map->PartialFunction {:fun        (fn [& args]
                                       (throw (RuntimeException. "Empty partial function.")))
                         :in-domain? (constantly false)}))