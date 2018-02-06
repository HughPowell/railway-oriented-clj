(ns uk.co.hughpowell.railway-oriented-clj.v1.public.adapters
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters :as adapters]))

(defn switch
  "An adapter that takes a regular function and turns it into a
  switch function.  The returned function encapsulates the result of
  the regular function in a successful result object."
  ([regular-fn]
    (switch regular-fn identity))
  ([regular-fn exception-handler]
   (adapters/switch regular-fn exception-handler)))

(defn tee
  "An adapter that takes a dead-end-fn and turns it into a switch
  function returning it's parameters as it's result."
  ([dead-end-fn]
    (tee dead-end-fn identity))
  ([dead-end-fn exception-handler]
   (adapters/switch
     (fn [& args]
       (apply dead-end-fn args)
       args)
     exception-handler)))

(defn bind
  "An adapter that takes a switch function and creates a bound
  function.  If the bound function is called with an arg that has a
  failure branch the arg is returned unchanged, otherwise the result of
  calling switch-fn on the arg is returned."
  [switch-fn]
  (adapters/bind switch-fn))

(defn lift
  "An adapter that takes a regular function and turns it into a
  bound function.  The returned function places the result of
  one-track-fn, if it is executed, on the success branch."
  [regular-fn]
  (adapters/lift regular-fn))
