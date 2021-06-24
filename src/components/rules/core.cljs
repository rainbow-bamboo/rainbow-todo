(ns components.rules.core
  (:require [odoyle.rules :as o]
            [components.rules.renders :as r]
            [components.rules.todos :as t]
            [components.rules.events :as e]
            [components.rules.closet :as c]
            [components.data.interface :as d]
            [clojure.edn :as edn]))

(def initial-session
  (-> (reduce o/add-rule (o/->session) (concat  t/todo-rules e/event-rules r/render-rules c/closet-rules))
      (o/insert ::t/global {::t/new-todo nil
                            ::t/active-id nil
                            ::t/edit-text ""})
      (o/insert ::t/derived {::t/todos []})
      (o/insert ::e/global {::e/next-id 1})
      (o/insert ::c/global {::c/inserted-passcode []
                            ::c/correct-passcode "PRIDE"
                            ::c/editing-passcode? false
                            ::c/valid-passcode? false})
      (o/insert ::c/secret {::c/content ""
                            ::c/editing? false})
      (o/insert ::c/gratitude {::c/content ""
                               ::c/editing? false})
      o/fire-rules))

(def hydrated-session 
  (let [facts (edn/read-string (d/get-item :facts))]
    (if facts
      (o/fire-rules
       (reduce o/insert initial-session facts))
      initial-session)))

(def *todo-session (atom hydrated-session))
