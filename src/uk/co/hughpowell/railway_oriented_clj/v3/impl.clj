(ns uk.co.hughpowell.railway-oriented-clj.v3.impl
  (:import (clojure.lang Cons)))

(def ^:private default-failure-handlers
  {:failure?                     (partial instance? Exception)
   :nil-handler                  (fn [] (NullPointerException.
                                          "Unexpected nil value encountered"))
   :unexpected-exception-handler identity
   :multiple-failure-handler     first})

(def failure-handlers
  (atom default-failure-handlers))

(defn get-failure?-fn []
  (:failure? @failure-handlers))

(defn get-nil-handler []
  (:nil-handler @failure-handlers))

(defn get-unexpected-exception-handler []
  (:unexpected-exception-handler @failure-handlers))

(defn get-multiple-failure-handler []
  (:multiple-failure-handler @failure-handlers))

(defn reset-failure-handlers []
  (reset! failure-handlers default-failure-handlers))

(defn is-fn? [sym]
  (and
    (-> sym resolve var?)
    (-> sym resolve meta :macro not)))

(defn wrap-form? [form]
  (and (or (list? form)
           (instance? Cons form))
       (or
         (-> form first symbol? not)
         (-> form first is-fn?))))
