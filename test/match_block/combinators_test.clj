(ns match-block.combinators-test
  (:use [match-block.core]
        [match-block.combinators]
        [midje.sweet]
        [match-block.test-util]))

(def sample-mblock-1
  (regular-fun-to-match-block +))

(def sample-mblock-2
  (match-block [x y]
               [1 2] "world"))

(def sample-mblock-3
  (regular-fun-to-match-block sample-mblock-2))

(facts-about regular-fun-to-match-block
             (sample-mblock-1 3 4 5) => 12
             (defined-at? sample-mblock-1 3 4 5) => true
             (= sample-mblock-3 sample-mblock-2) => true)

(def sample-mblock-4
  (with-nil-as-fallback sample-mblock-2))

(facts-about with-nil-as-fallback
             (sample-mblock-4 1 2) => "world"
             (sample-mblock-4 1 3) => nil)

(facts-about with-fallback-fn
             (with-fallback-fn sample-mblock-2 + 1 2) => "world"
             (with-fallback-fn sample-mblock-2 + 1 3) => 4)
