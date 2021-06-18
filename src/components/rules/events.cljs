(ns components.rules.events
  (:require [odoyle.rules :as o]
            [components.rules.todos :as t]))

(defn add-unique-id [coll]
  (map #(assoc  %1 :button/id %2)  coll (range (count coll))))

(defn create-buttons [text]
  (add-unique-id
   (map (fn [l] {:button/content l
                 :button/selected? false})
       (seq text))))


(defn toggle-select-button [id buttons]
  (update-in (vec buttons) [id :button/selected?] not))



(def event-rules
  (o/ruleset
   {::new-todo
    [:what
     [::insertion ::todo todo]
     [::global ::next-id next-id {:then false}]
     :then
     (o/insert! next-id {:todo/content todo
                         :todo/checked? false
                         :todo/editing? false
                         :todo/buttons (create-buttons todo)}) ;; Here we create the new todo with id
     (o/insert! ::t/global ::t/new-todo "") ;; This resets the input to be blank
     (o/insert! ::global ::next-id (inc next-id))]
    
    ::insert-passcode
    [:what
     [::insertion ::passcode selection]
     :then
     (let [{:keys [todo-id button-id buttons]} selection]
       (o/insert! todo-id {:todo/buttons (toggle-select-button button-id buttons)}))]})) 

