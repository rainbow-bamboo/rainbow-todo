(ns components.rules.events
  (:require [odoyle.rules :as o]
            [components.rules.todos :as t]))

(def event-rules
  (o/ruleset
   {::new-todo
    [:what
     [::insertion ::todo todo]
     [::global ::next-id next-id {:then false}]
     :then
     (o/insert! next-id {:todo/content todo
                         :todo/checked? false
                         :todo/editing? false}) ;; Here we create the new todo with id
     (o/insert! ::t/global ::t/new-todo "") ;; This resets the input to be blank
     (o/insert! ::global ::next-id (+ 1 next-id))]})) ;; this increments the id