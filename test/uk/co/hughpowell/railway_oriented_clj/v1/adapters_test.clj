(ns uk.co.hughpowell.railway-oriented-clj.v1.adapters-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test.alpha :as spec-test]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.verifiers :as verifiers]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.result :as result]))

(spec-test/instrument)

(deftest switch
  (testing
    "wrapping a regular function results in function that returns a
    successful result object"
    (let [data "foo"]
      (verifiers/verify-success data ((adapters/switch str) data))))
  (testing
    "wrapping a nil returning function results in a function that
     returns a failure result object."
    (verifiers/verify-exception
      (RuntimeException.)
      ((adapters/switch (constantly nil)) nil)))
  (testing
    "wrapping an exception throwing function results in a function that
    returns a failure result object."
    (let [message "foo"]
      (verifiers/verify-exception
        (RuntimeException. message)
        ((adapters/switch
           (fn [& args] (throw (RuntimeException. message)))) nil)))))

(deftest tee
  (testing
    "wrapping a function with one parameter results in a function that
    returns a successful result object with a single value."
    (let [param "foo"]
      (verifiers/verify-success
        [param]
        ((adapters/tee (constantly nil)) param))))
  (testing
    "wrapping a function with more than one parameter results in a
    function that returns a successful result object with a seq of the
    params as a value."
    (let [param1 "foo"
          param2 "bar"]
      (verifiers/verify-success
        [param1 param2]
        ((adapters/tee (constantly nil)) param1 param2)))))

(deftest bind
  (testing
    "binding a switch function results in a function that applies the
    value of a successful result object to the switch function."
    (verifiers/verify-success
      "foobar"
      ((adapters/bind (adapters/switch (partial str "foo")))
        (result/succeed "bar"))))
  (testing
    "binding a switch function results in a function that returns the
    given result object when it's a failure."
    (verifiers/verify-failure
      "fail"
      ((adapters/bind (adapters/switch (partial str "foo")))
        (result/fail "fail")))))

(deftest lift
  (testing
    "lifting a regular function results in a function that applies the
    value of a successful result object to the regular function."
    (let [data "foo"]
      (verifiers/verify-success
        "foobar"
        ((adapters/lift (partial str "foo"))
          (result/succeed "bar")))))
  (testing
    "lifting a regular function results in a function that returns the
    given result object when it's a failure."
    (verifiers/verify-failure
      "fail"
      ((adapters/lift (partial str "foo"))
        (result/fail "fail")))))
