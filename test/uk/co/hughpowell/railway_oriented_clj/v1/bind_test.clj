(ns uk.co.hughpowell.railway-oriented-clj.v1.bind-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test.alpha :as spec-test]
            [uk.co.hughpowell.railway-oriented-clj.v1.verifiers :as verifiers]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.bind :as bind]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.result :as result]))

(spec-test/instrument)

(deftest thread-first
  (testing
    "no forms returns a successful result object."
    (verifiers/verify-success
      "foo"
      (bind/-> "foo")))
  (testing
    "no forms returns a successful result object given a result object"
    (verifiers/verify-success
      "foo"
      (bind/-> (result/succeed "foo"))))
  (testing
    "one form returns the result as a success."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobar"
        (bind/-> "foo"
                 (switch-str "bar")))))
  (testing
    "two forms threads all the way through."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobarbaz"
        (bind/-> "foo"
                 (switch-str "bar")
                 (switch-str "baz")))))
  (testing
    "failure short circuits execution."
    (let [switch-str (adapters/switch str)
          failure-fn (constantly (result/fail "fail"))]
      (verifiers/verify-failure
        "fail"
        (bind/-> "foo"
                 (switch-str "bar")
                 (failure-fn)
                 (switch-str "baz")))))
  (testing
    "naked functions are threaded through."
    (let [switch-inc (adapters/switch inc)]
      (verifiers/verify-success
        3
        (bind/-> 1
                 switch-inc
                 switch-inc))))
  (testing
    "failure short circuits execution with naked functions."
    (let [switch-inc (adapters/switch inc)
          failure-fn (constantly (result/fail "fail"))]
      (verifiers/verify-failure
        "fail"
        (bind/-> 1
                 switch-inc
                 failure-fn
                 switch-inc)))))

(deftest thread-last
  (testing
    "no forms returns a successful result object."
    (verifiers/verify-success
      "foo"
      (bind/->> "foo")))
  (testing
    "one form returns the result as a success."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobar"
        (bind/->> "bar"
                  (switch-str "foo")))))
  (testing
    "two forms threads all the way through."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobarbaz"
        (bind/->> "baz"
                  (switch-str "bar")
                  (switch-str "foo")))))
  (testing
    "failure short circuits execution."
    (let [switch-str (adapters/switch str)
          failure-fn (constantly (result/fail "fail"))]
      (verifiers/verify-failure
        "fail"
        (bind/->> "foo"
                  (switch-str "bar")
                  (failure-fn)
                  (switch-str "baz")))))
  (testing
    "naked functions are threaded through."
    (let [switch-inc (adapters/switch inc)]
      (verifiers/verify-success
        3
        (bind/->> 1
                  switch-inc
                  switch-inc))))
  (testing
    "failure short circuits execution with naked functions."
    (let [switch-inc (adapters/switch inc)
          failure-fn (constantly (result/fail "fail"))]
      (verifiers/verify-failure
        "fail"
        (bind/->> 1
                  switch-inc
                  failure-fn
                  switch-inc)))))

(deftest thread-as
  (testing
    "no forms returns a successful result object."
    (verifiers/verify-success
      "foo"
      (bind/as-> "foo" $)))
  (testing
    "one form returns the result as a success."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobar"
        (bind/as-> "bar" $
                  (switch-str "foo" $)))))
  (testing
    "two forms threads all the way through."
    (let [switch-str (adapters/switch str)]
      (verifiers/verify-success
        "foobarbaz"
        (bind/as-> "bar" $
                  (switch-str "foo" $)
                  (switch-str $ "baz")))))
  (testing
    "failure short circuits execution."
    (let [switch-str (adapters/switch str)
          failure-fn (constantly (result/fail "fail"))]
      (verifiers/verify-failure
        "fail"
        (bind/as-> "bar" $
                  (switch-str "foo" $)
                  (failure-fn)
                  (switch-str $ "baz"))))))
