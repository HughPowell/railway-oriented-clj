(ns uk.co.hughpowell.railway-oriented-clj.v1.public.bind
  "Copyright (c) 2017.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:refer-clojure :exclude [-> ->> as->])
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.impl.arrange :as arrange]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.adapters :as adapters]
            [uk.co.hughpowell.railway-oriented-clj.v1.impl.result-object :as result]))

(defmacro ->
  "Threads the expr through the switch-fn-forms.  Wraps x as a success
  and inserts it as the second item in the first form, making a list of
  it if it is not a list already.  If there are more forms, inserts the
  first form as the second item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & switch-fn-forms]
  `(arrange/-> adapters/bind ~x ~@switch-fn-forms))

(defmacro ->>
  "Threads the expr through the switch-fn-forms.  Wraps x as a success
  and inserts it as the last item in the first form, making a list of
  it if it is not a list already. If there are more forms, inserts the
  first form as the last item in second form, etc.  Each form is
  wrapped to become a bound function resulting in execution stopping if
  an error is returned."
  [x & switch-fn-forms]
  `(arrange/->> adapters/bind ~x ~@switch-fn-forms))

(defmacro as->
  "Wraps each 
  form as a one parameter function using the name as the parameter.
  Threads expr and the resulting forms through arrange/-> with
  adapters/bind as f."
  [expr name & switch-fn-forms]
  (let [bound-forms (map (fn [form#]
                               `((fn [~name] ~form#)))
                             switch-fn-forms)]
    `(arrange/-> adapters/bind ~expr ~@bound-forms)))
