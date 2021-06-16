(ns components.rules.events
  (:require [odoyle.rules :as o]
            [components.rules.todos :as t]))

(def event-rules
  (o/ruleset
   {::new-todo
    [:what
     [::insertion ::todo todo]
     [::global ::next-id next-id {:then false}]
     [::t/global ::t/active-id active-id {:then false}]
     :then
     (let [id (if (nil? active-id)
                next-id 
                active-id)]
       (println "inserting todo with: " id todo)
       (o/insert! id {:todo/content todo
                      :todo/checked? false})
       (if (nil? active-id)
         (o/insert! ::global ::next-id (+ 1 next-id))
         (o/insert! ::t/active-id nil)))]}))