(ns components.rules.core
  (:require [odoyle.rules :as o]
            [components.rules.renders :as r]
            [components.rules.todos :as t]
            [components.rules.events :as e]))

(def initial-session
  (-> (reduce o/add-rule (o/->session) (concat  t/todo-rules e/event-rules r/render-rules))
      (o/insert ::t/global {::t/active-content ""
                            ::t/active-id nil})
      (o/insert ::t/derived {::t/todos []})
      (o/insert ::e/global {::e/next-id 1})
      o/fire-rules))

(def *todo-session (atom initial-session))
