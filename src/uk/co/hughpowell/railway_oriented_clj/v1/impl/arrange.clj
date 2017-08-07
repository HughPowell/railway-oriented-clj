(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.arrange
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:refer-clojure :exclude [-> ->>])
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result]))

(defmacro ->
  [f x switch-fn-forms]
  (loop [x (result/result true x), switch-fn-forms switch-fn-forms]
    (if switch-fn-forms
      (let [switch-fn-form (first switch-fn-forms)
            threaded (if (seq? switch-fn-form)
                       (with-meta `((~f #(~(first switch-fn-form)
                                           %
                                           ~@(next switch-fn-form)))
                                     ~x)
                                  (meta switch-fn-form))
                       (list (list `~f switch-fn-form) x))]
        (recur threaded (next switch-fn-forms)))
      x)))

(defmacro ->>
  [f x switch-fn-forms]
  (loop [x (result/result true x), switch-fn-forms switch-fn-forms]
    (if switch-fn-forms
      (let [switch-fn-form (first switch-fn-forms)
            threaded (if (seq? switch-fn-form)
                       (with-meta `((~f #(~(first switch-fn-form) ~@(next switch-fn-form) %)) ~x)
                                  (meta switch-fn-form))
                       (list (list `~f switch-fn-form) x))]
        (recur threaded (next switch-fn-forms)))
      x)))
