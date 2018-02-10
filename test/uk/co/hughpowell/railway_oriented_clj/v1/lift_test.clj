(ns uk.co.hughpowell.railway-oriented-clj.v1.lift-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test.alpha :as spec-test]
            [uk.co.hughpowell.railway-oriented-clj.v1.verifiers :as verifiers]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.lift :as lift]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.result :as result]))

(spec-test/instrument)

(deftest thread-first
  (testing
    "no forms returns a successful result object."
    (verifiers/verify-success
      "foo"
      (lift/-> "foo")))
  (testing
    "one form returns the result as a success."
    (verifiers/verify-success
      "foobar"
      (lift/-> "foo"
               (str "bar"))))
  (testing
    "two forms threads all the way through."
    (verifiers/verify-success
      "foobarbaz"
      (lift/-> "foo"
               (str "bar")
               (str "baz"))))
  (testing
    "failure short circuits execution."
    (let [failure-fn (constantly nil)]
      (verifiers/verify-exception
        (NullPointerException.)
        (lift/-> "foo"
                 (str "bar")
                 (failure-fn)
                 (str "baz")))))
  (testing
    "naked functions are threaded through."
    (verifiers/verify-success
      3
      (lift/-> 1
               inc
               inc)))
  (testing
    "failure short circuits execution with naked function."
    (let [failure-fn (constantly nil)]
      (verifiers/verify-exception
        (NullPointerException.)
        (lift/-> 1
                 inc
                 failure-fn
                 inc))))
  (testing
    "multiple result arguments"
    (let [one (result/succeed 1)
          switch-+ (adapters/switch +)]
      (verifiers/verify-success
        (result/succeed 3)
        (lift/-> 2
                 (switch-+ one))))))

(deftest thread-last
  (testing
    "no forms returns a successful result object."
    (verifiers/verify-success
      "foo"
      (lift/->> "foo")))
  (testing
    "one form returns the result as a success."
    (verifiers/verify-success
      "foobar"
      (lift/->> "bar"
                (str "foo"))))
  (testing
    "two forms threads all the way through."
    (verifiers/verify-success
      "foobarbaz"
      (lift/->> "baz"
                (str "bar")
                (str "foo"))))
  (testing
    "failure short circuits execution."
    (let [failure-fn (constantly nil)]
      (verifiers/verify-exception
        (NullPointerException.)
        (lift/->> "foo"
                  (str "bar")
                  (failure-fn)
                  (str "baz")))))
  (testing
    "naked functions are threaded through."
    (verifiers/verify-success
      3
      (lift/->> 1
                inc
                inc)))
  (testing
    "failure short circuits execution with naked functions."
    (let [failure-fn (constantly nil)]
      (verifiers/verify-exception
        (NullPointerException.)
        (lift/->> 1
                  inc
                  failure-fn
                  inc))))
  (testing
    "multiple result arguments"
    (let [one (result/succeed 1)
          switch-+ (adapters/switch +)]
      (verifiers/verify-success
        (result/succeed 3)
        (lift/->> 2
                 (switch-+ one))))))

(deftest thread-as
  (testing
    "no forms return a successful result object"
    (verifiers/verify-success
      "foo"
      (lift/as-> "foo" $)))
  (testing
    "one form returns the result as a success"
    (verifiers/verify-success
      "foobar"
      (lift/as-> "bar" $
                 (str "foo" $))))
  (testing
    "two forms threads all the way through."
    (verifiers/verify-success
      "foobarbaz"
      (lift/as-> "bar" $
                (str $ "baz")
                (str "foo" $))))
  (testing
    "failure short circuits execution."
    (let [failure-fn (constantly nil)]
      (verifiers/verify-exception
        (NullPointerException.)
        (lift/as-> "bar" $
                  (str $ "baz")
                  (failure-fn)
                  (str "foo" $))))))
