(ns components.rules.core
  (:require [odoyle.rules :as o]
            [components.rules.renders :as r]))

(def initial-session
  (-> (reduce o/add-rule (o/->session) (concat r/render-rules))
      o/fire-rules))

(def *todo-session (atom initial-session))