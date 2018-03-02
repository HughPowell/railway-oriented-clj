(ns uk.co.hughpowell.railway-oriented-clj.v3.core
  (:refer-clojure :exclude [-> ->> as-> comp when-let if-let])
  (:require [clojure.core :as core]
            [uk.co.hughpowell.railway-oriented-clj.v3.impl :as impl]))

;; Set up

(defn set-failure?-fn!
  "Sets the function that determines whether a form represents a
  failure."
  [failure?-fn]
  (swap! impl/failure-handlers assoc :failure? failure?-fn))

(defn set-exception-handler!
  "Sets the function that handles exceptions."
  [unexpected-exception-handler]
  (swap! impl/failure-handlers assoc :unexpected-exception-handler unexpected-exception-handler))

(defn set-nil-handler!
  "Sets the function that handles nil values."
  [nil-handler]
  (swap! impl/failure-handlers assoc :nil-handler nil-handler))

(defn set-multiple-failure-handler!
  "Set the function that handles multiple failures"
  [multiple-failure-handler]
  (swap! impl/failure-handlers
         assoc
         :multiple-failure-handler
         multiple-failure-handler))

;; Upgrades

(def wrap
  "Wrap the given function to return a failure should a failure be
  encountered."
  impl/wrap)

;; Sequential flow control

(defmacro ^{:private true} assert-args
  [& pairs]
  `(do (when-not ~(first pairs)
         (throw (IllegalArgumentException.
                  (str (first ~'&form) " requires " ~(second pairs) " in " ~'*ns* ":" (:line (meta ~'&form))))))
       ~(let [more (nnext pairs)]
          (when more
            (list* `assert-args more)))))

(defmacro ->
  "Thread first similar to the core macro, except that if a failure is
  discovered execution halts and the error is returned."
  [x & forms]
  (loop [value (impl/wrap-initial-thread-form x)
         forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta
                         (impl/wrap-form `(~(first form) ~value ~@(next form)))
                         (meta form))
                       (impl/wrap-form (list form value)))]
        (recur threaded (next forms)))
      `(impl/handle-nil ~value))))

(defmacro ->>
  "Thread last similar to the core macro, except that if a failure
  occurs execution halts and the error is returned."
  [x & forms]
  (loop [value (impl/wrap-initial-thread-form x)
         forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta
                         (impl/wrap-form `(~(first form) ~@(next form) ~value))
                         (meta form))
                       (impl/wrap-form (list form value)))]
        (recur threaded (next forms)))
      `(impl/handle-nil ~value))))

(defmacro as->
  "Thread 'as' similar to the core macro, except that if a failure
  occurs execution halts and the error is returned."
  [expr name & forms]
  `(let [~name ~(impl/wrap-form expr)
         ~@(interleave (repeat name) (map impl/wrap-form (butlast forms)))]
     ~(if (empty? forms)
        name
        (impl/wrap-form (last forms)))))

(defmacro when-let
  "Similar to the core when-let macro, except that multiple pairs are
  allowed (similar to 'let') and if a failure occurs in the binding
  forms execution halts and the error is returned."
  [bindings & exprs]
  (assert-args
    (vector? bindings) "a vector for its bindings"
    (even? (count bindings)) "an even number of forms in binding vector")
  (loop [bindings (reverse bindings)
         expr `(do ~@exprs)]
    (if (empty? bindings)
      expr
      (let [form (second bindings)
            threaded `(let [temp# ~(impl/wrap-form (first bindings))]
                        (if ((impl/get-failure?-fn) temp#)
                          temp#
                          (let [~form temp#]
                            ~expr)))]
        (recur (drop 2 bindings) threaded)))))

(defn comp
  "Similar to the core comp function, except that if a failure occurs
  execution halts and the failure is returned."
  [& fns]
  (apply core/comp (map wrap fns)))

;; Parallel flow control

(defn combine
  "Combine the results of multiple parallel calls that could fail.  If
  there are no failures the success-fn is called with the results as its
  arguments.  If there are one or more failures then the failure-fn is
  called with the list of results."
  ([results]
   (combine (impl/get-multiple-failure-handler) results))
  ([failure-fn results]
   (combine (fn [& args] args) failure-fn results))
  ([success-fn failure-fn results]
   (let [failures (impl/get-failures results)]
     (if (empty? failures)
       (apply success-fn results)
       (failure-fn failures)))))

;; Result Handlers

(defmacro if-let
  "Similar to the core if-let macro, except that the else branch is
  taken if the binding-form results in a failure."
  [bindings then else]
  (assert-args
    (vector? bindings) "a vector for its bindings"
    (= 2 (count bindings)) "exactly 2 forms in binding vector")
  (let [form (bindings 0)]
    `(let [result# ~(impl/wrap-form (bindings 1))
           ~form result#]
       (if ((impl/get-failure?-fn) result#)
         ~else
         ~then))))
