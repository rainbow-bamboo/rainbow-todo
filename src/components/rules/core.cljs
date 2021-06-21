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
      (o/insert ::t/global {::t/new-todo ""
                            ::t/active-id nil})
      (o/insert ::t/derived {::t/todos []})
      (o/insert ::e/global {::e/next-id 1})
      (o/insert ::c/global {::c/inserted-passcode []
                            ::c/correct-passcode "PRIDE"
                            ::c/valid-passcode? false})
      o/fire-rules))

(def *todo-session (atom initial-session))

(let [facts (edn/read-string (d/get-item :facts))]
  (println "inside read")
  (println facts)
  (if facts
    (swap! *todo-session
           (fn [session]
             (o/fire-rules
              (reduce o/insert session facts))))
    nil))



