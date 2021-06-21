(ns components.rules.renders
  (:require [odoyle.rules :as o]
            [odoyle.rum :as orum]
            [components.rules.todos :as t]
            [components.rules.events :as e]
            [components.rules.closet :as c]))

(defn insert! [*session id attr->value]
  (swap! *session
         (fn [session]
           (-> session
               (o/insert id attr->value)
               o/fire-rules))))

(defn rainbow-color []
  (let [colors ["red" "orange" "yellow" "green" "blue" "indigo" "violet"]]
    (rand-nth colors)))

(def render-rules
  (orum/ruleset
   {app-root
    [:then
     (let [*session (orum/prop)]
       [:div#app-root
        (closet-door *session)
        (closet *session)])]
    
    closet-door
    [:what
     [::c/global ::c/valid-passcode? isOpen?]
     :then
     (let [*session (orum/prop)]
       [:div#closet-door
        {:class (if isOpen? "open" "closed")}
        [:header
         [:h1
          {:on-click #(insert! *session ::e/closet {::e/close true})}
          "Rainbow Todo"]]
        (todo-form *session)
        (active-todos *session)
        #_(passcode-display *session)])]
    
    closet
    [:what
     [::c/global ::c/valid-passcode? isOpen?]
     :then
     (let [*session (orum/prop)]
       [:div#closet
        (if isOpen?
          [:h1
           {:on-click #(insert! *session ::e/closet {::e/close true})}
           "My Closet"]
          [:h1 "My Closet"])])]

    active-todos
    [:what
     [::t/derived ::t/todos todos]
     :then
     (let [*session (orum/prop)]
       [:ol
        (map (fn [t]
               [:li 
                {:id (str "todo-" (:id t))}
                [:input {:type "checkbox"
                         :class "todo-checkbox"
                         :checked (:checked? t)
                         :on-change #(insert! *session ::e/checkbox {::e/toggle (:id t)})}]
                (map (fn [b]
                           [:span.button
                            {:id (str "todo-" (:id t) "-letter-" (:button/id b))
                             :style (if (:button/selected? b)  {:color (rainbow-color)} nil)
                             :on-click #(insert! *session ::e/passcode {::e/insertion {:todo-id (:id t)
                                                                                       :button-id (:button/id b)
                                                                                       :button-content (:button/content b)
                                                                                       :buttons (:buttons t)}})}
                            (:button/content b)])
                         (:buttons t))])
             todos)])]

    todo-form
    [:what
     [::t/global ::t/new-todo todo]
     :then
     (let [*session (orum/prop)]
       [:div.shelf.new-todo-form
        [:input {:type "text"
                 :class "edit"
                 :placeholder "What needs to be done?"
                 :autoFocus true
                 :value todo
                 :on-change (fn [e]
                              (insert! *session ::t/global {::t/new-todo (-> e .-target .-value)}))
                 :on-key-down (fn [e]
                                (case (.-keyCode e)
                                  13
                                  (insert! *session ::e/todo {::e/insertion todo})
                                  nil))}]
        [:button {:on-click #(insert! *session ::e/todo {::e/insertion todo})}
         "New Todo"]])]

    passcode-display
    [:what
     [::c/global ::c/inserted-passcode passcode]
     [::c/global ::c/valid-passcode? isValid?]
     :then
     (let [*session (orum/prop)]
       [:div
        [:span (reduce str passcode)]])]}))
