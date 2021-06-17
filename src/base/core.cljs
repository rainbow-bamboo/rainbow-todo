(ns base.core
  (:require [rum.core :as rum]
            [odoyle.rules :as o]
            [components.rules.renders :as r]
            [components.rules.todos :as t]
            [components.rules.interface :as tr]))



(rum/defc app []
  (r/app-root tr/*todo-session))



(defn ^:export main
  []
  (rum/hydrate (app) (js/document.querySelector "#app")))
