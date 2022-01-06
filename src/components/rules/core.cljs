(ns components.rules.core
  (:require [odoyle.rules :as o]
            [components.rules.renders :as r]
            [components.rules.todos :as t]
            [components.rules.events :as e]
            [components.rules.vault :as v]
            [components.data.interface :as d]
            [clojure.edn :as edn]))

(def initial-session
  (-> (reduce o/add-rule (o/->session) (concat  t/todo-rules e/event-rules r/render-rules v/vault-rules))
      (o/insert ::t/global {::t/new-todo nil
                            ::t/active-id nil
                            ::t/edit-text ""})
      (o/insert ::t/derived {::t/todos []})
      (o/insert ::e/global {::e/next-id 1})
      (o/insert ::v/global {::v/inserted-passcode []
                            ::v/correct-passcode "PRIDE"
                            ::v/editing-passcode? false
                            ::v/valid-passcode? false})
      (o/insert ::v/secret {::v/content "I embody light"
                            ::v/editing? false})
      (o/insert ::v/gratitude {::v/content ""
                               ::v/editing? false})
      o/fire-rules))

(def hydrated-session 
  (let [facts (edn/read-string (d/get-item :facts))]
    (if facts
      (o/fire-rules
       (reduce o/insert initial-session facts))
      initial-session)))

(def *todo-session (atom hydrated-session))
