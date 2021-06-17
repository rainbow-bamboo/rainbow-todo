(ns components.rules.events
  (:require [odoyle.rules :as o]
            [components.rules.todos :as t]))

(defn create-buttons [text]
  (map (fn [l] {:button/content l
                :button/selected? false})
       (seq text)))


(def event-rules
  (o/ruleset
   {::new-todo
    [:what
     [::insertion ::todo todo]
     [::global ::next-id next-id {:then false}]
     :then
     (o/insert! next-id {:todo/content todo
                         :todo/checked? false
                         :todo/editing? false
                         :todo/buttons (create-buttons todo)}) ;; Here we create the new todo with id
     (o/insert! ::t/global ::t/new-todo "") ;; This resets the input to be blank
     (o/insert! ::global ::next-id (inc next-id))]})) 

