(ns uk.co.hughpowell.railway-oriented-clj.v1.impl.result-handlers
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [clojure.spec.alpha :as spec]
            [clojure.future :refer :all]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result-object]))

(defn succeeded?
  [{:keys [success?]}]
  success?)

(spec/fdef succeeded?
           :args (spec/cat :result ::result-object/result)
           :ret boolean?)

(def failed? (comp not succeeded?))

(defn success
  [{:keys [success]}]
  success)

(spec/fdef success
           :args (spec/cat :success ::result-object/success-result)
           :ret some?)

(defn failure
  [{:keys [failure]}]
  failure)

(spec/fdef failure
           :args (spec/cat :failure ::result-object/failure-result)
           :ret some?)

