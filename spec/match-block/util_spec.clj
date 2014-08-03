(ns match-block.util-spec
    (:require [speclj.core :refer :all]
    	      [match-block.util :refer :all]))


(describe "raise-error"
  (it "should raise runtime exception with message"
      (should-throw RuntimeException "with some message" 
         (raise-error "with some message"))))