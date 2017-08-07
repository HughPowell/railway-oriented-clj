(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers :as result-handlers]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result]))

(defn bind
  [switch-fn]
  (fn [result-object]
    (if (result-handlers/succeeded? result-object)
      (-> result-object result-handlers/success switch-fn)
      result-object)))

(defn switch
  [regular-fn]
  (fn [& regular-inputs]
    (try
      (result/result true (apply regular-fn regular-inputs))
      (catch Exception e (result/result false e)))))

(defn lift
  [regular-fn]
  (-> regular-fn
      switch
      bind))

