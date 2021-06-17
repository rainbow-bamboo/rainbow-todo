(ns components.rules.todos
  (:require [odoyle.rules :as o]))

(def todo-rules
  (o/ruleset
   {::todos
    [:what
     [id :todo/content content]
     [id :todo/checked? checked?]
     [id :todo/state state]
     :then-finally
     (->> (o/query-all o/*session* ::todos)
          (o/insert! ::derived ::todos))
     ]}))

