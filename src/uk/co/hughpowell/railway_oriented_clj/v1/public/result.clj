(ns uk.co.hughpowell.railway-oriented-clj.v1.public.result
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result-object]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers :as result-handlers]))

(defn succeed
  "Constructor for a successful result object."
  [value]
  (result-object/result true value))

(defn fail
  "Constructor for a failed result object."
  [error]
  (result-object/result false error))

(defn handle
  "Takes success and failure functions and returns a function that
  applies them, as appropriate, to the given result object."
  [success-fn failure-fn result]
  (if (result-handlers/succeeded? result)
    (-> result result-handlers/success success-fn)
    (-> result result-handlers/failure failure-fn)))

(defn combine
  "Applies each of the fns to the args.  If all the calls succeed, success-fn is
  called with of all the results as parameters.  If any of the calls fail,
  failure-fn is called with of all the failures as parameters."
  [success-fn failure-fn & fns]
  (let [f (apply juxt fns)]
    (fn [& args]
      (let [results (apply f args)
            failures (filter result-handlers/failed? results)]
        (if (empty? failures)
          (apply success-fn (map result-handlers/success results))
          (apply failure-fn (map result-handlers/failure failures)))))))
