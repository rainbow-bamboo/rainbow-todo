(ns components.rules.renders
  (:require [odoyle.rules :as o]
            [odoyle.rum :as orum]
            [components.rules.todos :as t]
            [components.rules.events :as e]))

(defn insert! [*session id attr->value]
  (swap! *session
         (fn [session]
           (-> session
               (o/insert id attr->value)
               o/fire-rules))))

(def render-rules
  (orum/ruleset
   {app-root
    [:then
    (let [*session (orum/prop)]
     [:div#app-root
      [:header
       [:h1 "This is my header"]
       (todo-input *session)
       (active-todos *session)]])]
    
    active-todos
    [:what
     [::t/derived ::t/todos todos]
     :then
     (let [*session (orum/prop)]
       [:ol
        (map (fn [t]
               [:li (:content t)]) todos)])]
    
    todo-input
    [:what
     [::t/global ::t/active-content content]
     :then
     (let [*session (orum/prop)]
       [:input {:type "text"
                :class "edit"
                :placeholder "What needs to be done?"
                :autoFocus true
                :value content
                :on-blur #(insert! *session ::e/insertion {::e/todo content})
                :on-change (fn [e]
                             (insert! *session ::t/global {::t/active-content (-> e .-target .-value)}))
                :on-key-down (fn [e]
                               (case (.-keyCode e)
                                 13
                                 (insert! *session ::e/insertion {::e/todo content})
                                 nil))}])]}))
