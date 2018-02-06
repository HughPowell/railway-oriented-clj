(ns uk.co.hughpowell.railway-oriented-clj.v1.public.debug
  (:require [uk.co.hughpowell.railway-oriented-clj.v1.public.result :as result]))

(defn spy [data]
  (clojure.pprint/pprint data)
  (result/succeed data))