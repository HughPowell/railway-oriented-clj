(ns uk.co.hughpowell.railway-oriented-clj.v2.core
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.arrange :as arrange]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers :as result-handlers]
            [clojure.core :as core])
  (:refer-clojure :exclude [-> ->> as-> some-> some->>]))

(defn fail 
  "Create failure result from the given failure.  The value will be a
  NullPointerException if failure is nil."
  [failure] (result/result false failure))

(defn succeed
  "Create success result from the given success.  A failure result will be
  created if success is nil."
  [success] (result/result true success))

(defn handle
  "Takes success and failure functions and returns a function that
  applies them, as appropriate, to the given result object."
  ([result]
   (handle identity identity result))
  ([success-fn failure-fn result]
   (if (result-handlers/succeeded? result)
     (core/-> result result-handlers/success success-fn)
     (core/-> result result-handlers/failure failure-fn))))

(defn wrap
  "Wrap a regular function into a ROC function.  Will always return success
  unless an exception is thrown or nil is returned in which case a failure
  is returned. The value of the failure is determined by applying the
  failure-fn to the exception (a NullPointerException in the case of returning
   nil).  failure-fn defaults to identity."
  ([f] (adapters/switch f identity))
  ([f failure-fn] (adapters/switch f failure-fn)))

(defn map-reduce
  "If all the results are successes then success-fn is called with all of the
  results as parameters.  If any of the results are failures, the failure-fn
  is called with all of the failures as parameters."
  [success-fn failure-fn & results]
  (let [failures (filter result-handlers/failed? results)]
    (if (empty? failures)
      (succeed (apply success-fn (map result-handlers/success results)))
      (fail (apply failure-fn (map result-handlers/failure failures))))))

(defn juxt-reduce
  "Applies each of the fns to the args.  If all the calls succeed, success-fn is
  called with of all the results as parameters.  If any of the calls fail,
  failure-fn is called with of all the failures as parameters."
  [success-fn failure-fn & fns]
  (let [f (apply juxt fns)]
    (fn [& args]
      (let [results (apply f args)]
        (apply map-reduce success-fn failure-fn results)))))

(defmacro ->
  "Threads the expr through the roc-fn-forms.  Wraps x as a success
  and inserts it as the second item in the first form, making a list of
  it if it is not a list already.  If there are more forms, inserts the
  first form as the second item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & roc-fn-forms]
  `(arrange/-> adapters/bind ~x ~@roc-fn-forms))

(defmacro ->>
  "Threads the expr through the roc-fn-forms.  Wraps x as a success
  and inserts it as the last item in the first form, making a list of
  it if it is not a list already. If there are more forms, inserts the
  first form as the last item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & roc-fn-forms]
  `(arrange/->> adapters/bind ~x ~@roc-fn-forms))

(defmacro as->
  "Wraps each 
  form as a one parameter function using the name as the parameter.
  Threads expr and the resulting forms through arrange/-> with
  adapters/bind as f."
  [expr name & roc-fn-forms]
  (let [forms-as-functions (map (fn [form#] `((fn [~name] ~form#)))
                                roc-fn-forms)]
    `(-> ~expr ~@forms-as-functions)))

(defmacro some->
  "Alias for ->, included for completeness."
  [x & switch-fn-forms]
  (-> ~x ~@switch-fn-forms))

(defmacro some->>
  "Alias for ->>, included for completness."
  [x & switch-fn-forms]
  (->> ~x ~@switch-fn-forms))

