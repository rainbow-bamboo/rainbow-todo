(ns dev.data
  (:require [components.rules.interface :as r]
            [components.rules.todos :as t]
            [odoyle.rules :as o]))

(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (.getItem (.-localStorage js/window) key))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

(set-item! :prefs "Hello world")
(get-item :prefs)

(def facts (->> (o/query-all @r/*todo-session)
                (remove (fn [[id]]
                          (= id ::t/derived)))))

