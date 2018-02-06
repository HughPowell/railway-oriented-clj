(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [clojure.spec.alpha :as spec]
            [clojure.future :refer :all]))

(spec/def ::success? boolean?)
(spec/def ::value some?)
(spec/def ::result some?)

(defrecord Result [success? value])
(defn- succeed
  [value]
  (->Result true value))

(spec/fdef succeed
           :args (spec/cat :value some?)
           :ret ::result)

(defn- fail
  [failure]
  (->Result false failure))

(spec/fdef fail
           :args (spec/cat :failure some?)
           :ret ::result)

(defn result [success? data]
  (if (some? data)
    (if success?
      (succeed data)
      (fail data))
    (fail (NullPointerException.))))

(spec/fdef result
           :args (spec/cat :success? boolean? :data any?)
           :ret ::result)
