(ns uk.co.hughpowell.railway-oriented-clj.v1.result-handlers-test
  (:require [clojure.test :refer :all]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers :as result-handlers]))

(deftest success
  (testing 
    "regression test for weird inputs"
    (is (= (result-handlers/success (list {})) (list {})))))