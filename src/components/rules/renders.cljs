(ns components.rules.renders
  (:require [odoyle.rules :as o]
            [odoyle.rum :as orum]
            [components.rules.todos :as t]
            [components.rules.events :as e]
            [components.rules.closet :as c]
            [clojure.string :as s]))

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
          [:div
           [:h1
            {:on-click #(insert! *session ::e/closet {::e/close true})}
            "My Closet"]
           (passcode-display *session)]
          [:h1 "My Closet"])])]

    active-todos
    [:what
     [::t/derived ::t/todos todos]
     [::t/global ::t/edit-text edit-text]
     :then
     (let [*session (orum/prop)
           sorted-todos (sort-by :checked? todos)]
       [:div.todo-list
        [:ol
         (map (fn [t]
                (if (:editing? t)
                  [:li
                   {:id (str "todo-" (:id t))
                    :class (if (:checked? t) "checked" nil)}
                   [:input {:type "text"
                            :class "edit"
                            :placeholder (if (not (:content t))
                                          "What needs to be done"
                                          nil)
                            :autoFocus true
                            :value (:content t)
                            :on-blur #(insert! *session ::e/todo {::e/edit-complete (:id t)})
                            :on-change (fn [e]
                                         (insert! *session (:id t) {:todo/content (-> e .-target .-value)}))
                            :on-key-down (fn [e]
                                           (case (.-keyCode e)
                                             13
                                             (insert! *session ::e/todo {::e/edit-complete (:id t)})
                                             nil))}]
                   [:button
                    {:on-click #(insert! *session ::e/todo {::e/edit-complete (:id t)})}
                    "Save"]]
                  [:li
                   {:id (str "todo-" (:id t))
                    :class (if (:checked? t) "checked" nil)}
                   [:input {:type "checkbox"
                            :class "todo-checkbox"
                            :checked (:checked? t)
                            :on-change #(insert! *session ::e/checkbox {::e/toggle (:id t)})}]
                   (map (fn [b]
                          [:span.button
                           {:id (str "todo-" (:id t) "-letter-" (:button/id b))
                            :style (if (:button/selected? b)  {:background-color (rainbow-color)} nil)
                            :on-click #(insert! *session ::e/passcode {::e/insertion {:todo-id (:id t)
                                                                                      :button-id (:button/id b)
                                                                                      :button-content (:button/content b)
                                                                                      :buttons (:buttons t)}})}
                           (:button/content b)])
                        (:buttons t))
                   [:button
                    {:on-click #(insert! *session ::e/todo {::e/start-edit (:id t)})}
                    "Edit"]]))
              sorted-todos)]
        (if (seq (filter :checked? todos))
          [:button
           {:on-click #(insert! *session ::e/todo {::e/clear-checked true})}
           "Clear Checked"]
          nil)])]

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
     [::c/global ::c/correct-passcode passcode]
     [::c/global ::c/editing-passcode? editing?]
     :then
     (let [*session (orum/prop)]
       (if editing?
         [:div
          [:input {:type "text"
                   :placeholder "change your passcode"
                   :autoFocus true
                   :value passcode
                   :on-blur #(insert! *session ::c/global {::c/editing-passcode? false})
                   :on-change (fn [e]
                                (insert! *session ::c/global { ::c/correct-passcode (s/upper-case (-> e .-target .-value))}))
                   :on-key-down (fn [e]
                                  (case (.-keyCode e)
                                    13
                                    (insert! *session ::c/global {::c/editing-passcode? false})
                                    nil))}]
          [:button {:on-click #(insert! *session ::c/global {::c/editing-passcode? false})}
           "Done"]]
         [:div
          [:span.passcode passcode]
          [:button {:on-click #(insert! *session ::c/global {::c/editing-passcode? true})}
           "Edit"]
          ]))]}))


#_(def todos [{:checked? true :content "Hello"} {:checked? false :content "tunder"} {:checked? true :content "last"}])

#_(reverse (sort-by :checked? todos))