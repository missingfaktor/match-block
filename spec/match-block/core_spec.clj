(ns match-block.core-spec
  (:require [speclj.core :refer :all]
	    [clojure.core.match :only (match)]
	    [match-block.core :refer :all]))

(def foo
   (match-block [a b]
		[3 1] :nice
		:else :aww-shucks))

(def bar 
   (match-block [a b]
   		[3 1] :works))

(describe "match-block"
   (it "returns value for defined nodes"
      (should= :nice (foo 3 1)))

   (it "evaluates else block for not defined nodes"
      (should= :aww-shucks (foo :not :defined)))
   
   (it "raises exception for non defined nodes if else block is not present"
      (should-throw IllegalArgumentException "No matching clause: 3 4" (bar 3 4))))

(describe "defined-at?"
  (it "evaluates to true if match block is defined for given nodes"
     (should= true (defined-at? foo 3 1)))
 
  (it "evaluates to true if match block is defined for given nodes"
     (should= true (defined-at? foo 3 3)))

  (it "evaluates to false if match block is not defined for given nodes"
     (should= false (defined-at? bar 3 3))))

(run-specs)
