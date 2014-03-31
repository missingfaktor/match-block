(ns match-block.core-test
  (:use [midje.sweet]
        [match-block.core]
        [match-block.test-util]))

(def sample-mblock
  (match-block [x y]
               [3 :a] :hello
               [4 :b] :world))

(def another-sample-mblock
  (match-block [x y]
               :else :always))

(facts-about "match blocks"
             (sample-mblock 3 :a) => :hello
             (sample-mblock 4 :b) => :world
             (sample-mblock :whoopty :do) => (throws Exception)
             (another-sample-mblock :whoopty :do) => :always
             (defined-at? sample-mblock 3 :a) => true
             (defined-at? sample-mblock 4 :b) => true
             (defined-at? sample-mblock :whoopty :do) => false
             (defined-at? another-sample-mblock :whoopty :do) => true)

(facts-about empty-match-block
             (empty-match-block 4 5 6) => (throws RuntimeException)
             (defined-at? empty-match-block 4 5 6) => false)