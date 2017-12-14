(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [clojure.spec.alpha :as spec]
            [clojure.future :refer :all]))

(spec/def ::success? boolean?)
(spec/def ::success some?)
(spec/def ::success-result (spec/keys :req-un [::success?
                                               ::success]))

(defn- succeed
  [value]
  {:success? true
   :success  value})

(spec/fdef succeed
           :args (spec/cat :value some?)
           :ret ::success-result)

(spec/def ::failure some?)
(spec/def ::failure-result (spec/keys :req-un [::success?
                                               ::failure]))

(defn- fail
  [failure]
  {:success? false
   :failure  failure})

(spec/fdef fail
           :args (spec/cat :failure some?)
           :ret ::failure-result)

(spec/def ::result (spec/or :success ::success-result
                            :failure ::failure-result))

(defn result
  [success? data]
  (if (some? data)
    (if success?
      (succeed data)
      (fail data))
    (fail (NullPointerException.))))

(spec/fdef result
           :args (spec/cat :success? boolean? :data any?)
           :ret ::result)
