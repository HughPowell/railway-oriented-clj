(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers :as result-handlers]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result])
  (:import (uk.co.hughpowell.railway_oriented_clj.v1.impl.result_object Result)))

(defn bind
  [switch-fn]
  (fn [result-object & params]
    (if (result-handlers/failed? result-object)
      (if (nil? result-object) (result/result false nil) result-object)
      (let [result-objects (if (empty? params) [result-object] params)
            failures (filter (fn [result]
                               (if (instance? Result result)
                                 (not (:success? result))
                                 false))
                             result-objects)]
        (if (empty? failures)
          (apply switch-fn (map result-handlers/success result-objects))
          (result/result false (map (fn [failure] (if (nil? failure)
                                                    (NullPointerException.)
                                                    (result-handlers/failure failure)))
                                    failures)))))))

(defn switch
  [regular-fn exception-handler]
  (fn [& regular-inputs]
    (try
      (result/result true (apply regular-fn regular-inputs))
      (catch Exception e (result/result false (exception-handler e))))))

(defn lift
  ([regular-fn]
   (lift regular-fn identity))
  ([regular-fn exception-handler]
   (-> regular-fn
       (switch exception-handler)
       bind)))

