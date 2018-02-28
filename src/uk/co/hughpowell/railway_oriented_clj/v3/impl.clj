(ns uk.co.hughpowell.railway-oriented-clj.v3.impl)

(def ^:private default-failure-handlers
  {:failure?                     (partial instance? Exception)
   :nil-handler                  (fn [] (NullPointerException.))
   :unexpected-exception-handler identity})

(def failure-handlers
  (atom default-failure-handlers))

(defn get-failure?-fn []
  (:failure? @failure-handlers))

(defn get-nil-handler []
  (:nil-handler @failure-handlers))

(defn get-unexpected-exception-handler []
  (:unexpected-exception-handler @failure-handlers))

(defn reset-failure-handlers []
  (reset! failure-handlers default-failure-handlers))
