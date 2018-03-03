(ns uk.co.hughpowell.railway-oriented-clj.v3.impl
  (:require [clojure.string :as string])
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

(defn handle-nil [value]
  (if (some? value)
    value
    ((get-nil-handler))))

(defn get-failures [params]
  (->> params
       (map handle-nil)
       (filter (get-failure?-fn))))

(defn- wrap-symbol? [sym]
  (letfn [(reserved? [sym]
            (or (special-symbol? sym)
                (string/starts-with? (str sym) ".")
                (string/ends-with? (str sym) ".")))
          (macro? [sym]
            (-> sym resolve meta :macro))]
    (or (keyword? sym)
        (and
          (not (reserved? sym))
          (not (macro? sym))))))

(defn- wrap-form? [form]
  (and (or (list? form)
           (instance? Cons form))
       (or
         (-> form first symbol? not)
         (-> form first wrap-symbol?))))

(defn wrap
  ([f]
    (wrap f (get-unexpected-exception-handler)))
  ([f exception-handler]
   (fn [& args]
     (let [failures (get-failures args)]
       (if (empty? failures)
         (try
           (handle-nil (apply f args))
           (catch Exception e (exception-handler e)))
         ((get-multiple-failure-handler) failures))))))

(defn wrap-form [form]
  (if (wrap-form? form)
    (cons (list wrap (first form)) (next form))
    form))

(defn wrap-callable-form [form]
  (cond
    (wrap-form? form) (cons (list wrap (first form)) (next form))
    (and (not (list? form)) (wrap-symbol? form)) (list (list wrap form))
    :else form))

(defmacro wrap-forms [forms]
  `(try
     (handle-nil ~forms)
     (catch Exception e#
       ((get-unexpected-exception-handler) e#))))

