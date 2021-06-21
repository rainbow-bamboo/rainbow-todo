(ns components.data.interface
  (:require [components.data.localstorage :as l]))

(defn set-item! [key val]
  (l/set-item! key val))

(defn get-item [key]
  (l/get-item key))

(defn remove-item! [key]
  (l/remove-item! key))