(ns match-block.core-test
  (:use [midje.sweet]
        [match-block.core]
        [match-block.test-util]))

(def sample-pfun (partial-fn [x y]
                             [3 :a] :hello
                             [4 :b] :world))

(def another-sample-pfun (partial-fn [x y]
                                     :else :always))

(facts-about "partial functions"
             (sample-pfun 3 :a) => :hello
             (sample-pfun 4 :b) => :world
             (sample-pfun :whoopty :do) => (throws Exception)
             (another-sample-pfun :whoopty :do) => :always
             (in-domain? sample-pfun 3 :a) => true
             (in-domain? sample-pfun 4 :b) => true
             (in-domain? sample-pfun :whoopty :do) => false
             (in-domain? another-sample-pfun :whoopty :do) => true)

(facts-about define-partial-fn
             (define-partial-fn foo [a]
                                [:a] :ok
                                :else :ko) =expands-to=> (def foo
                                                           (match-block.core/partial-fn [a]
                                                                                       [:a] :ok
                                                                                       :else :ko)))

(facts-about empty-partial-fn
             (empty-partial-fn 4 5 6) => (throws RuntimeException)
             (in-domain? empty-partial-fn 4 5 6) => false)