(ns match-block.ast-transformations-test
  (:use [midje.sweet]
        [match-block.ast-transformations]
        [match-block.test-util]))

(def matcher-1 [[1 2] "x"])
(def matcher-2 [[2 3] "y"])
(def matcher-3 [:else "z"])

(facts-about separate-matchers
             (separate-matchers [matcher-1 matcher-2 matcher-3]) => [[matcher-1 matcher-2] "z"]
             (separate-matchers [matcher-1 matcher-2]) => [[matcher-1 matcher-2] nil])

(facts-about match-fn-block->node
             (match-fn-block->node '(fn [x y]
                                      (match [x y]
                                             [3 :a] :yes
                                             [4 :b] :umm-maybe
                                             :else :no))) => {:node-type :match-fn
                                                              :args-vec  '[x y]
                                                              :matchers  [[[3 :a] :yes]
                                                                          [[4 :b] :umm-maybe]]
                                                              :else-part :no}
             (match-fn-block->node '(fn [x y]
                                      (match [x y]
                                             [3 :a] :yes
                                             [4 :b] :umm-maybe))) => {:node-type :match-fn
                                                                      :args-vec  '[x y]
                                                                      :matchers  [[[3 :a] :yes]
                                                                                  [[4 :b] :umm-maybe]]
                                                                      :else-part nil})

(facts-about create-in-domain?-node
             (create-in-domain?-node {:node-type :match-fn
                                      :args-vec  '[x y]
                                      :matchers  [[[3 :a] :yes]
                                                  [[4 :b] :umm-maybe]]
                                      :else-part :no}) => {:node-type      :in-domain?-fn
                                                           :args-vec       '[x y]
                                                           :explicit-cases [[3 :a] [4 :b]]
                                                           :has-else?      true}
             (create-in-domain?-node {:node-type :match-fn
                                      :args-vec  '[x y]
                                      :matchers  [[[3 :a] :yes]
                                                  [[4 :b] :umm-maybe]]
                                      :else-part nil}) => {:node-type      :in-domain?-fn
                                                           :args-vec       '[x y]
                                                           :explicit-cases [[3 :a] [4 :b]]
                                                           :has-else?      false})

(facts-about in-domain?-node->block
             (in-domain?-node->block {:node-type      :in-domain?-fn
                                      :args-vec       '[x y]
                                      :explicit-cases [[3 :a] [4 :b]]
                                      :has-else?      true}) => '(clojure.core/fn [x y] true)
             (in-domain?-node->block {:node-type      :in-domain?-fn
                                      :args-vec       '[x y]
                                      :explicit-cases [[3 :a] [4 :b]]
                                      :has-else?      false}) => '(clojure.core/fn [x y]
                                                                    (clojure.core.match/match [x y]
                                                                                              [3 :a] true
                                                                                              [4 :b] true
                                                                                              :else false)))

(facts-about match-fn-block->in-domain?-block
             (match-fn-block->in-domain?-block '(fn [x y]
                                                  (match [x y]
                                                         [3 :a] :yes
                                                         [4 :b] :umm-maybe))) => '(clojure.core/fn [x y]
                                                                                    (clojure.core.match/match [x y]
                                                                                                              [3 :a] true
                                                                                                              [4 :b] true
                                                                                                              :else false)))

