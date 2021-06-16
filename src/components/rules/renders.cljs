(ns components.rules.renders
  (:require [odoyle.rules :as o]
            [odoyle.rum :as orum]))

(defn insert! [*session id attr->value]
  (swap! *session
         (fn [session]
           (-> session
               (o/insert id attr->value)
               o/fire-rules))))

(def render-rules
  (orum/ruleset
   {app-root
    [:then
    (let [*session (orum/prop)]
     [:div#app-root
      [:header
       [:h1 "This is my header"]]])]}))