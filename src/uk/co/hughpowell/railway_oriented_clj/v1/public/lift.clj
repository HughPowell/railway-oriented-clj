(ns uk.co.hughpowell.railway-oriented-clj.v1.public.lift
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:refer-clojure :exclude [-> ->> as-> some-> some->>])
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.arrange :as arrange]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result]
            [uk.co.hughpowell.railway-oriented-clj.v1.public.bind :as bind]))

(defmacro ->
  "Threads the expr through the regular-fn-forms.  Wraps x as a success
  and inserts it as the second item in the first form, making a list of
  it if it is not a list already.  If there are more forms, inserts the
  first form as the second item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & regular-fn-forms]
  `(arrange/-> adapters/lift ~x ~@regular-fn-forms))

(defmacro ->>
  "Threads the expr through the regular-fn-forms.  Wraps x as a success
  and inserts it as the last item in the first form, making a list of
  it if it is not a list already. If there are more forms, inserts the
  first form as the last item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & regular-fn-forms]
  `(arrange/->> adapters/lift ~x ~@regular-fn-forms))

(defmacro as->
  "Wraps each forms function as a switch function.  Wraps each resulting
  form as a one parameter function using the name as the parameter.
  Threads expr and the resulting forms through arrange/-> with
  adapters/bind as f."
  [expr name & regular-fn-forms]
  (let [forms-as-functions (map (fn [form#] `((fn [~name] ~form#)))
                                regular-fn-forms)]
    `(-> ~expr ~@forms-as-functions)))

(defmacro some->
  [x & regular-fn-forms]
  (-> ~x ~@regular-fn-forms))

(defmacro some->>
  [x & regular-fn-forms]
  (->> ~x ~@regular-fn-forms))
