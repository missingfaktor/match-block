(ns match-block.core
  (:import [clojure.lang IFn])
  (:use [clojure.core.match :only [match]]
        [match-block.util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; MatchBlock definition

(defrecord MatchBlock [defined-at? fun]
  IFn
  (invoke [this a]
    (fun a))
  (invoke [this a b]
    (fun a b))
  (invoke [this a b c]
    (fun a b c))
  (invoke [this a b c d]
    (fun a b c d))
  (invoke [this a b c d e]
    (fun a b c d e))
  (invoke [this a b c d e f]
    (fun a b c d e f))
  (invoke [this a b c d e f g]
    (fun a b c d e f g))

  ; Arity >= 7 not supported. Because I am feeling too lazy to write that boilerplate.
  ; And who'd need it anyway?

  (applyTo [this args]
    (apply fun args)))

(defn defined-at? [mblock & args]
  (apply (:defined-at? mblock) args))

(defn match-block? [obj]
  (instance? MatchBlock obj))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; AST nodes and transformations

(defprotocol Node
  (to-clj [_]))

(defrecord MatchFnNode [args-vec matchers else-part]
  Node
  (to-clj [_]
    `(fn ~args-vec
       (match ~args-vec
              ~@(apply concat matchers)
              ~@(when else-part
                  [:else else-part])))))

(defrecord DefinedAtNode [explicit-cases has-else? args-vec]
  Node
  (to-clj [_]
    (if has-else?
      `(fn ~args-vec true)
      `(fn ~args-vec
         (match ~args-vec
                ~@(mapcat vector explicit-cases (repeat true))
                :else false)))))

(defrecord MatchBlockNode [match-fn-node defined-at?-node]
  Node
  (to-clj [_]
    `(map->MatchBlock {:fun ~(to-clj match-fn-node)
                       :defined-at? ~(to-clj defined-at?-node)})))

(defn match-fn-node->defined-at?-node [{:keys [args-vec matchers else-part] :as match-fn-node}]
  (let [explicit-cases (map first matchers)
        has-else? (boolean else-part)]
    (DefinedAtNode. explicit-cases has-else? args-vec)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Macro

(defn separate-matchers [ms]
  (let [last-clause (last ms)]
    (if (= (first last-clause) :else)
      [(butlast ms) (last last-clause)]
      [ms nil])))

(defmacro match-block [args-vec & all-matchers]
  (let [[matchers else-part] (separate-matchers (partition-all 2 all-matchers))
        match-fn-node (MatchFnNode. args-vec matchers else-part)
        defined-at?-node (match-fn-node->defined-at?-node match-fn-node)
        match-block-node (MatchBlockNode. match-fn-node defined-at?-node)]
    (to-clj match-block-node)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Functions and combinators

(def empty-match-block
  (map->MatchBlock {:fun         (fn [& _]
                                   (raise-error "Empty match block."))
                    :defined-at? (constantly false)}))

(defn regular-fun-to-match-block [fun]
  (if (match-block? fun)
    fun
    (map->MatchBlock {:fun         fun
                      :defined-at? (constantly true)})))

(defn with-nil-as-fallback [mblock]
  (fn [& args]
    (if (apply defined-at? mblock args)
      (apply mblock args)
      nil)))

(defn with-fallback-fn [mblock fallback-fn & args]
  (let [fn-to-invoke (if (apply defined-at? mblock args)
                       mblock
                       fallback-fn)]
    (apply fn-to-invoke args)))
