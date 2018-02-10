(ns uk.co.hughpowell.railway-oriented-clj.v2.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [uk.co.hughpowell.railway-oriented-clj.v2.core :as roc]))

(def blank-name-text "Names must not be blank")
(defn validate-name-text [{:keys [name] :as input}]
  (if (string/blank? name)
    (roc/fail blank-name-text)
    (roc/succeed input)))

(def too-long-name-text "Name must not be longer than 50 chars")
(defn validate-name-length [{:keys [name] :as input}]
  (if (> (count name) 50)
    (roc/fail too-long-name-text)
    (roc/succeed input)))

(def blank-email-text "Email must not be blank")
(defn validate-email-text [{:keys [email] :as input}]
  (if (string/blank? email)
    (roc/fail blank-email-text)
    (roc/succeed input)))

(defn expected-failure [expected-failure result]
  (roc/handle
    (fn [_] (is false))
    (fn [failure] (is (= failure expected-failure)))
    result))

(defn expected-exception-failure [expected-failure result]
  (roc/handle
    (fn [_] (is false))
    (fn [failure] (is (= (type failure) expected-failure)))
    result))

(defn expected-success [expected-success result]
  (roc/handle
    (fn [success] (is (= expected-success success)))
    (fn [_] (is false))
    result))

(deftest empty-name-empty-email
  (testing "empty name and empty email short circuits on name check."
    (expected-failure
      blank-name-text
      (roc/-> {:name "" :email ""}
              validate-name-text
              validate-name-length
              validate-email-text))))

(deftest legal-name-empty-email
  (testing "legal name and empty email short circuits on email check."
    (expected-failure
      blank-email-text
      (roc/-> {:name "Alice" :email ""}
              validate-name-text
              validate-name-length
              validate-email-text))))

(defn canonicalise-email [{:keys [email] :as input}]
  (->> email
       string/trim
       string/lower-case
       (assoc input :email)))

(deftest canonicalise-good-result
  (testing "canonicalise email when given legal data."
    (expected-success
      {:name "Alice" :email "uppercase"}
      (roc/-> {:name "Alice" :email "UPPERCASE "}
              validate-name-text
              validate-name-length
              validate-email-text
              canonicalise-email))))

(deftest canonicalise-bad-result
  (testing "short circuiting when given illegal data."
    (expected-failure
      blank-name-text
      (roc/-> {:name "" :email "UPPERCASE "}
              validate-name-text
              validate-name-length
              validate-email-text
              canonicalise-email))))

(def nil-returner (constantly nil))

(deftest handle-nil
  (testing "nil returning functions."
    (expected-exception-failure
      NullPointerException
      (roc/-> {:name "Alice" :email "good"}
              validate-name-text
              validate-name-length
              nil-returner
              validate-email-text))))

(defn exception-thrower [& _]
  (throw (RuntimeException.)))

(deftest handle-exception
  (testing "exception throwing function."
    (let [exception-thrower (roc/wrap exception-thrower)]
      (expected-exception-failure
        RuntimeException
        (roc/-> {:name "Alice" :email "good"}
                validate-name-text
                validate-name-length
                exception-thrower
                validate-email-text))))
  
  (testing "exception throwing function using custom error handler."
    (let [error-handler (fn [e] {:type  :exception
                                 :error e})
          exception-thrower (roc/wrap exception-thrower error-handler)]
      (roc/handle
        (fn [_] (is false))
        (fn [{:keys [type error]}]
          (is (and (= type :exception)
                   (instance? RuntimeException error))))
        (roc/-> {:name "Alice" :email "good"}
                validate-name-text
                validate-name-length
                exception-thrower
                validate-email-text)))))

(deftest map-reduce
  (let [parallel-validation (partial roc/map-reduce
                              (fn [& args] (apply merge args))
                              (fn [& args] (apply str (interpose "; " args))))]
    (testing "2 failures"
      (expected-failure
        (str blank-name-text "; " blank-email-text)
        (parallel-validation
          (validate-name-text {:name "" :email ""})
          (validate-name-length {:name "" :email ""})
          (validate-email-text {:name "" :email ""}))))
    
    (testing "1 failure"
      (expected-failure
        blank-email-text
        (parallel-validation
          (validate-name-text {:name "Alice" :email ""})
          (validate-name-length {:name "Alice" :email ""})
          (validate-email-text {:name "Alice" :email ""}))))
    
    (testing "success"
      (expected-success
        {:name "Alice" :email "good"}
        (parallel-validation 
          (validate-name-text {:name "Alice" :email "good"})
          (validate-name-length {:name "Alice" :email "good"})
          (validate-email-text {:name "Alice" :email "good"}))))))

(deftest juxt-reduce
  (let [parallel-validation (roc/juxt-reduce
                              (fn [& args] (apply merge args))
                              (fn [& args] (apply str (interpose "; " args)))
                              validate-name-text
                              validate-name-length
                              validate-email-text)]
    (testing "2 failures"
      (expected-failure
        (str blank-name-text "; " blank-email-text)
        (parallel-validation {:name "" :email ""})))
    
    (testing "1 failure"
      (expected-failure
        blank-email-text
        (parallel-validation {:name "Alice" :email ""})))
    
    (testing "success"
      (expected-success
        {:name "Alice" :email "good"}
        (parallel-validation {:name "Alice" :email "good"})))))
