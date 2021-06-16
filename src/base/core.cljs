(ns base.core
  (:require [rum.core :as rum]))



(rum/defc app []
  [:h1 "Hello TODO"])


(defn ^:export main
  []
  (rum/hydrate (app) (js/document.querySelector "#app")))
