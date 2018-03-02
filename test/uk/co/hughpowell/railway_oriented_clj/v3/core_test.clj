(ns uk.co.hughpowell.railway-oriented-clj.v3.core-test
  (:require [clojure.test :refer :all]
            [uk.co.hughpowell.railway-oriented-clj.v3.core :as roc]
            [uk.co.hughpowell.railway-oriented-clj.v3.impl :as impl])
  (:refer-clojure :exclude [comp when-let if-let]))

(deftest setting-failure-handlers
  (testing
    "Setting handlers to change failure objects"
    (try
      (roc/set-failure?-fn! (fn [result] (#{:nil :unexpected-exception} result)))
      (roc/set-nil-handler! (fn [] :nil))
      (roc/set-exception-handler! (fn [_] :unexpected-exception))
      (is (= (roc/-> 1 ((fn [_] (throw (RuntimeException.))))) :unexpected-exception))
      (is (= (let [nil-failure nil] 
               (roc/-> nil-failure ((fn [_] nil)))) :nil))
      (finally
        (impl/reset-failure-handlers)))))

(deftest thread-first
  (testing
    "A single success should be returned"
    (is (= (roc/-> {}) {})))
  (testing
    "A single failure should be returned"
    (let [failure (RuntimeException.)]
      (is (= (roc/-> failure) failure))))
  (testing
    "A starting nil value should be returned as a failure"
    (let [nil-failure nil]
      (is (instance? NullPointerException (roc/-> nil-failure)))))
  (testing
    "A single function with all successful parameters should return the
    result"
    (is (= (roc/-> (- 2 1)) 1)))
  (testing
    "Allow evaluation of railway oriented clj macro"
    (is (= (roc/-> 1 inc (roc/-> dec)) 1)))
  (testing
    "A single function with a failed parameter should return the failure."
    (let [failure (RuntimeException.)]
      (is (= (roc/-> (- 2 failure)) failure))))
  (testing
    "Threading a success into a bare function"
    (is (= (roc/-> 1 integer?) true)))
  (testing
    "Threading a failure into a bare function"
    (let [failure (RuntimeException.)]
      (is (= (roc/-> failure integer?) failure))))
  (testing
    "Threading a success into a multi-parameter function"
    (is (= (roc/-> 2 (- 1)) 1)))
  (testing
    "Threading a failure into a mullti-parameter function"
    (let [failure (RuntimeException.)]
      (is (= (roc/-> failure (- 1)) failure))))
  (testing
    "Threading a success into a multi-parameter function containing
    a failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/-> 2 (- failure)) failure))))
  (testing
    "Allowing macro forms inside the thread"
    (is (= (roc/-> :a name (or nil)) "a")))
  (testing
    "Allowing bare macros inside the thread"
    (is (= (roc/-> 1 and) 1)))
  (testing
    "Allowing bare keywords inside the thread"
    (is (= (roc/-> {:a 1} :a) 1)))
  (testing
    "Local bindings are wrapped"
    (let [failure (RuntimeException.)
          do-something (constantly failure)
          do-something-else (fn [_] :fail)]
      (is (= (roc/-> (do-something)
                     do-something-else)
             failure)))))

(deftest thread-last
  (testing
    "A single success should be evaluated and returned"
    (is (= (roc/->> {}) {})))
  (testing
    "A single failure should be evaluated and returned"
    (let [failure (RuntimeException.)]
      (is (= (roc/->> failure) failure))))
  (testing
    "A starting nil value should be returned as a failure"
    (let [nil-failure nil]
      (is (instance? NullPointerException (roc/->> nil-failure)))))
  (testing
    "A single function with all successful parameters should return the
    evaluated result"
    (is (= (roc/->> (- 2 1)) 1)))
  (testing
    "Allow evaluation of railway oriented clj macro"
    (is (= (roc/->> inc (roc/-> 1)) 2)))
  (testing
    "A single function with a failed parameter should return that
    failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/->> (- 2 failure)) failure))))
  (testing
    "Threading a success into a bare function"
    (is (= (roc/->> 1 integer?) true)))
  (testing
    "Threading a failure into a bare function returns that failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/->> failure integer?) failure))))
  (testing
    "Threading a success into a multi-parameter function"
    (is (= (roc/->> 1 (- 2)) 1)))
  (testing
    "Threading a failure into a multi-parameter function"
    (let [failure (RuntimeException.)]
      (is (= (roc/-> failure (- 1)) failure))))
  (testing
    "Threading a success into a multi-parameter function containing
    a failure should return that failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/->> 2 (- failure)) failure))))
  (testing
    "Allowing macro forms inside the thread"
    (is (= (roc/->> :a name (and "b")) "a")))
  (testing
    "Allowing bare macros inside the thread"
    (is (= (roc/->> 1 and) 1)))
  (testing
    "Allowing bare keywords inside the thread"
    (is (= (roc/->> {:a 1} :a) 1)))
  (testing
    "Local bindings are wrapped"
    (let [failure (RuntimeException.)
          do-something (constantly failure)
          do-something-else (fn [_] :fail)]
      (is (= (roc/->> (do-something)
                      do-something-else)
             failure)))))

(deftest thread-as
  (testing
    "A single success should be evaluated and returned"
    (is (= (roc/as-> {} $) {})))
  (testing
    "A single failure should be returned"
    (let [failure (RuntimeException.)]
      (is (= (roc/as-> failure $) failure))))
  (testing
    "A starting nil value should be returned as a failure"
    (let [nil-failure nil]
      (is (instance? NullPointerException (roc/as-> nil-failure $)))))
  (testing
    "A single function with all successful parameters should return the
    evaluated the result"
    (is (= (roc/as-> (- 2 1) $) 1)))
  (testing
    "Allow evaluation of railway oriented clj macro"
    (is (= (roc/as-> 1 $
                     (roc/-> 1 (+ $))
                     (roc/->> $ (+ 2))) 4)))
  (testing
    "A single function with a failed parameter should return that
    failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/as-> (- 1 failure) $) failure))))
  (testing
    "Threading into a function with all successful parameters should
    evaluated that function"
    (is (= (roc/as-> 2 $
                     (+ $ 1 $)) 5)))
  (testing
    "Threading into a function with a failed parameter should return
    that failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/as-> 2 $
                       (+ failure $))))))
  (testing
    "Allowing macro forms inside the thread"
    (is (= (roc/as-> 1 $
                     (inc $)
                     (and $ "a"))
           "a"))))

(deftest wrap
  (testing
    "Wrapping a function with the default exception handler"
    (let [failure (RuntimeException.)]
      (is (= ((roc/wrap (fn [] (throw failure)))) failure))))
  (testing
    "Wrapping a function with a custom exception handler"
    (is (= ((roc/wrap (fn [] (throw (RuntimeException.)))
                      (fn [_] :exception)))
           :exception)))
  (testing
    "Wrapping a function and calling it with a nil parameter create a
    nil error"
    (is (instance? NullPointerException ((roc/wrap (fn [_] 1)) nil))))
  (testing
    "Wrapping a function and calling it with a failure parameter returns
    that failure"
    (let [failure (RuntimeException.)]
      (is (= ((roc/wrap (fn [_] 1)) failure) failure))))
  (testing
    "Wrapping a function and calling it with multiple failures returns
    the first failure"
    (let [first-failure (RuntimeException.)]
      (is (= ((roc/wrap (fn [_ _ _] 1))
               first-failure
               (Exception.)
               (NullPointerException.))
             first-failure)))))

(deftest when-let
  (testing
    "Execution completes if the assignment is a success"
    (is (= (roc/when-let [x 1] x) 1)))
  (testing
    "Execution completes if the assignment is a successful function
    call"
    (is (= (roc/when-let [x (+ 1 2)] x) 3)))
  (testing
    "Allow evaluation of railway oriented clj macros"
    (is (= (roc/when-let [x (roc/-> 1 inc (+ 2))] x) 4)))
  (testing
    "Return the failure when a failure occurs"
    (let [failure (RuntimeException.)]
      (is (= (roc/when-let [x failure
                            y 1]
               (+ x y)) failure))))
  (testing
    "Halt execution if a failure occurs"
    (let [failure (RuntimeException.)]
      (is (= (roc/when-let [x failure
                            y (#(throw (Exception.)))]
               (+ x y))
             failure))))
  (testing
    "Return nil failure when nil encountered"
    (let [nil-failure nil]
      (is (instance? NullPointerException
                     (roc/when-let [x nil-failure
                                    y 1]
                       (+ x y)))))))

(deftest comp
  (testing
    "Return success when every function succeeds"
    (is (= ((roc/comp inc (partial - 15) +) 4 5) 7)))
  (testing
    "Return the failure if any of the functions fail"
    (let [failure (RuntimeException.)]
      (is (= ((roc/comp (fn [_] 2) (fn [_] failure) identity) 1) failure)))))

(deftest combine
  (testing
    "All successes should call success with all successes as parameters"
    (is (= (roc/combine [1 2 3 4]) [1 2 3 4])))
  (testing
    "A nil result should be returned as a failure"
    (is (instance? NullPointerException (roc/combine [1 2 nil 4]))))
  (testing
    "One failure should return that failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/combine [1 2 failure 4]) failure))))
  (testing
    "Multiple failures should return the first failure"
    (let [first-failure (RuntimeException.)
          second-failure (NullPointerException.)]
      (is (= (roc/combine [1 first-failure second-failure 2])
             first-failure)))))

(deftest if-let
  (testing
    "Execute the 'then' branch when the test is a success"
    (is (= (roc/if-let [x :success]
                       x
                       :fail)
           :success)))
  (testing
    "Allow execution of railway oriented clj macros"
    (is (= (roc/if-let [x (roc/-> 1 inc (+ 2))]
                       x
                       :fail))))
  (testing
    "Execute the 'else' branch when the test is a failure"
    (let [failure (RuntimeException.)]
      (is (= (roc/if-let [x failure]
                         :fail
                         x)
             failure))))
  (testing
    "Execute the 'else' branch when the result is nil"
    (is (instance? NullPointerException (let [nil-failure nil] (roc/if-let [x nil-failure]
                                                                           :success
                                                                           x)))))
  (testing
    "Allow use of macros inside the binding"
    (is (= (roc/if-let [result (case :three
                                 :one "one"
                                 :two "two"
                                 "many")]
                       result
                       :failure)
           "many"))))

