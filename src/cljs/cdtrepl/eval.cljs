(ns cdtrepl.eval
  (:require 
    [clojure.walk :as cw]
    [cdtrepl.background :as background]
    [khroma.log :as log]
    [khroma.util :as kutil]
    [khroma.extension :as extension]
    [cljs.core.async :as async])

  (:require-macros 
    [cljs.core.async.macros :refer [go alt! go-loop]]))

(defn ok-result [request result] 
  (assoc request :eval-status "ok" :eval-result result))

(defn error-result [request message] 
  (assoc request :eval-status "error" :eval-message message))

(defn evaluator [in-ch]
  (let [out-ch (async/chan)]
    (go-loop [request (<! in-ch)]
      ;(log/debug "cdt evaluator < " request)

      (let [result (<! (background/eval (:js-statement request) (:response-ns request)))]
        ;(log/debug "cdt eveluator < " result)
        (>! out-ch
          (if-not (:exception result)
            (ok-result request result)
            (error-result request (:message result)))))

      (recur (<! in-ch)))
    out-ch))


