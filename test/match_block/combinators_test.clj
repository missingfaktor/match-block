(ns match-block.combinators-test
  (:use [match-block.core]
        [match-block.combinators]
        [midje.sweet]
        [match-block.test-util]))

(def sample-pfun-1
  (fn->partial-fn +))

(define-partial-fn sample-pfun-2 [x y]
                   [1 2] "world")

(def sample-pfun-3
  (fn->partial-fn sample-pfun-2))

(facts-about fn->partial-fn
             (sample-pfun-1 3 4 5) => 12
             (in-domain? sample-pfun-1 3 4 5) => true
             (= sample-pfun-3 sample-pfun-2) => true)

(def sample-pfun-4
  (fallback-to-nil sample-pfun-2))

(facts-about fallback-to-nil
             (sample-pfun-4 1 2) => "world"
             (sample-pfun-4 1 3) => nil)

(facts-about invoke-with-fallback-fn
             (invoke-with-fallback-fn sample-pfun-2 + 1 2) => "world"
             (invoke-with-fallback-fn sample-pfun-2 + 1 3) => 4)
