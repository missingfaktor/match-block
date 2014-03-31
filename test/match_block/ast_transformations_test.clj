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

(facts-about create-defined-at?-node
             (create-defined-at?-node {:node-type :match-fn
                                       :args-vec  '[x y]
                                       :matchers  [[[3 :a] :yes]
                                                   [[4 :b] :umm-maybe]]
                                       :else-part :no}) => {:node-type      :defined-at?-fn
                                                            :args-vec       '[x y]
                                                            :explicit-cases [[3 :a] [4 :b]]
                                                            :has-else?      true}
             (create-defined-at?-node {:node-type :match-fn
                                       :args-vec  '[x y]
                                       :matchers  [[[3 :a] :yes]
                                                   [[4 :b] :umm-maybe]]
                                       :else-part nil}) => {:node-type      :defined-at?-fn
                                                            :args-vec       '[x y]
                                                            :explicit-cases [[3 :a] [4 :b]]
                                                            :has-else?      false})

(facts-about defined-at?-node->block
             (defined-at?-node->block {:node-type      :defined-at?-fn
                                       :args-vec       '[x y]
                                       :explicit-cases [[3 :a] [4 :b]]
                                       :has-else?      true}) => '(clojure.core/fn [x y] true)
             (defined-at?-node->block {:node-type      :defined-at?-fn
                                       :args-vec       '[x y]
                                       :explicit-cases [[3 :a] [4 :b]]
                                       :has-else?      false}) => '(clojure.core/fn [x y]
                                                                     (clojure.core.match/match [x y]
                                                                                               [3 :a] true
                                                                                               [4 :b] true
                                                                                               :else false)))

(facts-about match-fn-block->defined-at?-block
             (match-fn-block->defined-at?-block '(fn [x y]
                                                   (match [x y]
                                                          [3 :a] :yes
                                                          [4 :b] :umm-maybe))) => '(clojure.core/fn [x y]
                                                                                     (clojure.core.match/match [x y]
                                                                                                               [3 :a] true
                                                                                                               [4 :b] true
                                                                                                               :else false)))

