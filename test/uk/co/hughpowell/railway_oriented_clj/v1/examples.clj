(ns uk.co.hughpowell.railway-oriented-clj.v1.examples
  (:refer-clojure :exclude [->])
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.core :as core]
            [clojure.spec.test.alpha :as spec-test]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.result :as result]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.bind :as bind]
            [uk.co.hughpowell.railway-oriented-clj.v1.verifiers :as verifiers]))

(spec-test/instrument)

(def blank-name-text "Names must not be blank")
(defn validate-1 [{:keys [name] :as input}]
  (if (string/blank? name)
    (result/fail blank-name-text)
    (result/succeed input)))

(def too-long-name-text "Name must not be longer than 50 chars")
(defn validate-2 [{:keys [name] :as input}]
  (if (> (count name) 50)
    (result/fail too-long-name-text)
    (result/succeed input)))

(def blank-email-text "Email must not be blank")
(defn validate-3 [{:keys [email] :as input}]
  (if (string/blank? email)
    (result/fail blank-email-text)
    (result/succeed input)))

(deftest empty-name-empty-email
  (testing "empty name and empty email short circuits on name check."
    (verifiers/verify-failure
      blank-name-text
      (bind/-> {:name "" :email ""}
               validate-1
               validate-2
               validate-3))))

(deftest legal-name-empty-email
  (testing "legal name and empty email short circuits on email check."
    (verifiers/verify-failure
      blank-email-text
      (bind/-> {:name "Alice" :email ""}
               validate-1
               validate-2
               validate-3))))

(deftest legal-name-legal-email
  (testing "legal data is successfully returned."
    (let [data {:name "Alice" :email "good"}]
      (verifiers/verify-success
        data
        (bind/-> data
                 validate-1
                 validate-2
                 validate-3)))))

(defn canonicalise-email [{:keys [email] :as input}]
  (->> email
       string/trim
       string/lower-case
       (assoc input :email)))

(deftest canonicalise-good-result
  (testing "canonicalise email when given legal data."
    (verifiers/verify-success
      {:name "Alice" :email "uppercase"}
      (bind/-> {:name "Alice" :email "UPPERCASE "}
               validate-1
               validate-2
               validate-3
               ((adapters/switch canonicalise-email))))))

(deftest canonicalise-bad-result
  (testing "short circuit when given illegal data."
    (verifiers/verify-failure
      blank-name-text
      (bind/-> {:name "" :email "UPPERCASE "}
               validate-1
               validate-2
               validate-3
               ((adapters/switch canonicalise-email))))))

(deftest lift-canonicalise-email
  (testing "bind switch functions, lift regular functions and use normal
  threading."
    (verifiers/verify-success
      {:name "Alice" :email "uppercase"}
      (core/-> {:name "Alice" :email "UPPERCASE "}
               validate-1
               ((adapters/bind validate-2))
               ((adapters/bind validate-3))
               ((adapters/lift canonicalise-email))))))

(defn exception-thrower [& _]
  (throw (RuntimeException.)))

(deftest handle-exception
  (testing "handle exception throwing function."
    (verifiers/verify-exception
      (RuntimeException.)
      (bind/-> {:name "Alice" :email "good"}
               validate-1
               validate-2
               ((adapters/switch exception-thrower))
               validate-3))))

(defn update-database [_])

(deftest tee
  (testing "tee function."
    (verifiers/verify-success
      [{:name "Alice" :email "good"}]
      (bind/-> {:name "Alice" :email "good"}
               validate-1
               validate-2
               validate-3
               ((adapters/tee update-database))))))

(def debug-success-text "DEBUG. Success so far:")
(def debug-failure-text "ERROR.")

(def log
  (partial
    result/handle
    (fn [value] (println debug-success-text value) (result/succeed value))
    (fn [error] (println debug-failure-text error) (result/fail error))))

(defn use-case [data]
  (log
    (bind/-> data
             validate-1
             validate-2
             validate-3
             ((adapters/switch canonicalise-email))
             ((adapters/tee update-database)))))

(deftest use-case-test
  (testing "valid input"
    (let [data {:name "Alice" :email "good"}]
      (is (= (with-out-str
               (println debug-success-text (list data)))
             (with-out-str
               (verifiers/verify-success
                 [data]
                 (use-case data)))))))
  (testing "invalid input"
    (is (= (with-out-str
             (println debug-failure-text blank-name-text))
           (with-out-str
             (verifiers/verify-failure
               blank-name-text
               (use-case {:name "" :email ""})))))))

(defn add-success [head & _]
  (result/succeed head))

(defn add-failure [& args]
  (result/fail (apply str (interpose "; " args))))

(def combined-validator
  (result/combine
    add-success
    add-failure
    validate-1
    validate-2
    validate-3))

(deftest combine
  (testing "2 failures"
    (verifiers/verify-failure
      (str blank-name-text "; " blank-email-text)
      (combined-validator {:name "" :email ""})))
  (testing "1 failure"
    (verifiers/verify-failure
      blank-email-text
      (combined-validator {:name "Alice" :email ""})))
  (testing "valid input"
    (let [data {:name "Alice" :email "good"}]
      (verifiers/verify-success
        data
        (combined-validator data)))))

(defn combine-use-case [data]
  (bind/-> data
           combined-validator
           ((adapters/switch canonicalise-email))
           ((adapters/tee update-database))))

(deftest combine-use-case-test
  (testing "valid input"
    (verifiers/verify-success
      [{:name "Alice" :email "uppercase"}]
      (combine-use-case {:name "Alice" :email "UPPERCASE "}))))