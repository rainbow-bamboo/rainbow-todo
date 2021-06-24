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
  (let [colors ["#FFB3BA" "#FFDFBB" "#FFFFBA" "#BAFFC9" "#BAFFC9" "#BAE1FF"]]
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
           (passcode-display *session)
           (secret *session)
           (gratitude *session)
           [:button 
            {:on-click #(insert! *session ::e/closet {::e/close true})}
            "Close"]]
          [:h1 "My Closet"])])]

    active-todos
    [:what
     [::t/derived ::t/todos todos]
     [::t/global ::t/edit-text edit-text]
     :then
     (let [*session (orum/prop)
           sorted-todos (->> todos
                             (sort-by :id)
                             reverse
                             (sort-by :checked?))]
       [:div.todo-list
        [:ul
         (map (fn [t]
                (if (:editing? t)
                  [:li
                   {:id (str "todo-" (:id t))
                    :class (if (:checked? t) "checked" nil)}
                   [:input {:class "color-picker"
                            :type "color"
                            :name "todo-color"
                            :value (:color t)
                            :on-change (fn [e]
                                         (insert! *session (:id t) {:todo/color (-> e .-target .-value)}))}]
                   [:input {:type "text"
                            :class "edit-todo"
                            :placeholder (if (not (:content t))
                                           "New Todo"
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
                   [:span
                    {:class ["save-button" "material-icons"]
                     :on-click #(insert! *session ::e/todo {::e/edit-complete (:id t)})}
                    "done"]]
                  
                  ; ELSE

                  [:li
                   {:id (str "todo-" (:id t))
                    :class (if (:checked? t) "checked" nil)}
                   [:div.todo
                    [:input {:class "color-picker"
                             :type "color"
                             :name "todo-color"
                             :value (:color t)
                             :on-change (fn [e]
                                          (insert! *session (:id t) {:todo/color (-> e .-target .-value)}))}]
                    (if (:checked? t)
                      [:span {:class ["todo-checkbox" "material-icons"]
                              :on-click #(insert! *session ::e/checkbox {::e/toggle (:id t)})}
                       "check_box"]
                      [:span {:class ["todo-checkbox" "material-icons"]
                              :on-click #(insert! *session ::e/checkbox {::e/toggle (:id t)})}
                       "check_box_outline_blank"])

                    (map (fn [b]
                           [:span
                            {:id (str "todo-" (:id t) "-letter-" (:button/id b))
                             :class ["button" (if (= (:button/content b) " ") "space" nil)]
                             :style (if (:button/selected? b)  {:background-color (rainbow-color)} nil)
                             :on-click #(if (not (:button/selected? b))
                                          (insert! *session ::e/passcode {::e/insertion {:todo-id (:id t)
                                                                                         :button-id (:button/id b)
                                                                                         :button-content (:button/content b)
                                                                                         :buttons (:buttons t)}})
                                          nil)}
                            (:button/content b)])
                         (:buttons t))]
                   [:span
                    {:class ["edit-button" "material-icons"]
                     :on-click #(insert! *session ::e/todo {::e/start-edit (:id t)})}
                    "edit"]]))
              sorted-todos)]
        (if (seq (filter :checked? todos))
          [:button
           {:class "clear-button"
            :on-click #(insert! *session ::e/todo {::e/clear-checked true})}
           "Clear Checked"]
          nil)])]

    todo-form
    [:what
     [::t/global ::t/new-todo todo]
     :then
     (let [*session (orum/prop)]
       [:div.shelf.new-todo-form
        [:input {:type "text"
                 :class "edit-todo"
                 :placeholder "New Todo"
                 :autoFocus true
                 :value todo
                 :on-change (fn [e]
                              (insert! *session ::t/global {::t/new-todo (-> e .-target .-value)}))
                 :on-key-down (fn [e]
                                (case (.-keyCode e)
                                  13
                                  (insert! *session ::e/todo {::e/insertion todo})
                                  nil))}]
        [:button {:class "material-icons"
                  :on-click #(insert! *session ::e/todo {::e/insertion todo})}
         "add"]])]

    secret
    [:what
     [::c/secret ::c/content content]
     [::c/secret ::c/editing? editing?]
     :then
     (let [*session (orum/prop)]
       (if editing?
         [:div
          [:label {:for "secret-text"} "My Secret:"]
          [:textarea {:name "secret-text"
                      :placeholder "What's weighing on your heart?"
                      :rows 7
                      :value content
                      :on-change (fn [e]
                                   (insert! *session ::c/secret {::c/content  (-> e .-target .-value)}))
                      :on-blur #(insert! *session ::c/secret {::c/editing? false})}]
          [:button {:on-click #(insert! *session ::c/secret {::c/editing? false})}
           "Done"]]
         [:div.secret-display
          content
          [:button {:on-click #(insert! *session ::c/secret {::c/editing? true})}
           "Edit"]]))]

    gratitude
    [:what
     [::c/gratitude ::c/content content]
     [::c/gratitude ::c/editing? editing?]
     :then
     (let [*session (orum/prop)]
       (if editing?
         [:div
          [:label {:for "gratitude-text"} "I'm grateful for:"]
          [:textarea {:name "secret-text"
                      :placeholder "Something that makes your smile"
                      :rows 7
                      :value content
                      :on-change (fn [e]
                                   (insert! *session ::c/gratitude {::c/content  (-> e .-target .-value)}))
                      :on-blur #(insert! *session ::c/gratitude {::c/editing? false})}]
          [:button {:on-click #(insert! *session ::c/gratitude {::c/editing? false})}
           "Done"]]
         [:div.gratitude-display
          content
          [:button {:on-click #(insert! *session ::c/gratitude {::c/editing? true})}
           "Edit"]]))]

    passcode-display
    [:what
     [::c/global ::c/correct-passcode passcode]
     [::c/global ::c/editing-passcode? editing?]
     :then
     (let [*session (orum/prop)]
       (if editing?
         [:div
          [:p "The code to your closet is:"]
          [:input {:type "text"
                   :placeholder "change your passcode"
                   :autoFocus true
                   :value passcode
                   :on-blur #(insert! *session ::c/global {::c/editing-passcode? false})
                   :on-change (fn [e]
                                (insert! *session ::c/global {::c/correct-passcode (s/upper-case (-> e .-target .-value))}))
                   :on-key-down (fn [e]
                                  (case (.-keyCode e)
                                    13
                                    (insert! *session ::c/global {::c/editing-passcode? false})
                                    nil))}]
          [:button {:on-click #(insert! *session ::c/global {::c/editing-passcode? false})}
           "Done"]]
         [:div
          [:span "The code to your closet is:"]
          [:span.passcode passcode]
          [:button {:on-click #(insert! *session ::c/global {::c/editing-passcode? true})}
           "Edit"]]))]}))


#_(def todos [{:checked? true :content "Hello"} {:checked? false :content "tunder"} {:checked? true :content "last"}])

#_(reverse (sort-by :checked? todos))