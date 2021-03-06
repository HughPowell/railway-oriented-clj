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

(defn- wrap-list-form? [form]
  (and (or (list? form)
           (instance? Cons form))
       (or
         (-> form first symbol? not)
         (-> form first wrap-symbol?))))

(defn- wrap-bare-form? [form]
  (and (not (list? form)) (wrap-symbol? form)))

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

(defn wrap-form
  ([form]
   (if (wrap-list-form? form)
     (cons (list wrap (first form)) (next form))
     form))
  ([form sym]
  (cond
    (wrap-list-form? form) (cons (list wrap (first form)) (next form))
    (wrap-bare-form? form) (list (list wrap form))
    :else `(if ((get-failure?-fn) ~sym)
             ~sym
             ~form)))
  ([form sym thread]
  (cond
    (wrap-list-form? form) (list thread sym (cons (list wrap (first form)) (next form)))
    (wrap-bare-form? form) (list thread sym (list (list wrap form)))
    :else `(if ((get-failure?-fn) ~sym)
             ~sym
             ~(list thread sym form)))))

(defmacro wrap-forms [forms]
  `(try
     (handle-nil ~forms)
     (catch Exception e#
       ((get-unexpected-exception-handler) e#))))
