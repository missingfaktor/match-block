(ns match-block.ast-transformations
  (:use [match-block.util]
        [clojure.core.match :only [match]]))

(defn separate-matchers [ms]
  (let [last-clause (last ms)]
    (if (= (first last-clause) :else)
      [(butlast ms) (last last-clause)]
      [ms nil])))

(defn match-fn-block->node [match-fn-block]
  (let [[_ args-vec match-block] match-fn-block
        [_ _ & rest] match-block
        [matchers else-part] (separate-matchers (partition-all 2 rest))]
    (into {:node-type :match-fn}
          (make-keyword-map args-vec matchers else-part))))

(defn create-defined-at?-node [match-fn-node]
  (let [explicit-cases (map first (:matchers match-fn-node))
        has-else? (boolean (:else-part match-fn-node))
        args-vec (:args-vec match-fn-node)]
    (into {:node-type :defined-at?-fn}
          (make-keyword-map explicit-cases has-else? args-vec))))

(defn defined-at?-node->block [node]
  (let [syms-vec (:args-vec node)]
    (if (:has-else? node)
      `(fn ~syms-vec true)
      `(fn ~syms-vec
         (match ~syms-vec
                ~@(reduce concat (map vector (:explicit-cases node) (repeat true)))
                :else false)))))

(defn match-fn-block->defined-at?-block [match-fn-block]
  (-> match-fn-block
      match-fn-block->node
      create-defined-at?-node
      defined-at?-node->block))
