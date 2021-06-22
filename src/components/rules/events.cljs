(ns components.rules.events
  (:require [odoyle.rules :as o]
            [components.rules.todos :as t]
            [components.rules.closet :as c]
            [components.data.interface :as d]
            [clojure.string :as s]
            [clojure.edn :as edn]))

(defn add-unique-id [coll]
  (map #(assoc  %1 :button/id %2)  coll (range (count coll))))

(defn create-buttons [text]
  (add-unique-id
   (map (fn [l] {:button/content l
                 :button/selected? false})
       (seq text))))


(defn select-button [id buttons]
  (update-in (vec buttons) [id] assoc :button/selected? true))


(def event-rules
  (o/ruleset
   {::new-todo
    [:what
     [::todo ::insertion todo]
     [::global ::next-id next-id {:then false}]
     :then
     (o/insert! next-id {:todo/content todo
                         :todo/checked? true
                         :todo/editing? false
                         :todo/buttons (create-buttons todo)}) ;; Here we create the new todo with id
     (o/insert! ::t/global ::t/new-todo "") ;; This resets the input to be blank
     (o/insert! ::global ::next-id (inc next-id))]
    
    ::insert-passcode
    [:what
     [::passcode ::insertion selection]
     [::c/global ::c/inserted-passcode passcode {:then false}]
     :then
     (let [{:keys [todo-id button-id button-content buttons]} selection]
       (o/insert! todo-id {:todo/buttons (select-button button-id buttons)})
       (o/insert! ::c/global ::c/inserted-passcode (conj passcode (s/upper-case button-content))))]
    
    ::close-closet
    [:what
     [::closet ::close true]
     [id :todo/content todo {:then false}]
     [id :todo/buttons buttons {:then false}]
     :then
     (o/insert! ::c/global ::c/inserted-passcode [])
     (o/insert! ::c/global ::c/valid-passcode? false)
     (o/insert! id :todo/buttons (create-buttons todo))]
    
    ::toggle-checkbox
    [:what
     [::checkbox ::toggle todo-id]
     [todo-id :todo/checked? isChecked? {:then false}]
     :then
     (o/insert! todo-id :todo/checked? (not isChecked?))]
    
    ::start-edit
    [:what
     [::todo ::start-edit todo-id]
     [todo-id :todo/editing? isEditing? {:then false}]
     [todo-id :todo/content content {:then false}]
     :then
     (o/insert! todo-id :todo/editing? true)
     (o/insert! ::t/global ::t/edit-text content)]
    
    ::edit-complete
    [:what
     [::todo ::edit-complete todo-id]
     [todo-id :todo/content content]
     :then
     (o/insert! todo-id :todo/editing? false)
     (o/insert! todo-id :todo/buttons (create-buttons content))
     (o/insert! ::closet ::close true)]
    
    ::read-from-storage
    [:what
     [::c/global ::c/correct-passcode "PRIDE"]]
    
    ::save-to-localstorage
    [:what
     [id :todo/checked? checked]
     [id :todo/content content]
     :then
     (println "inside save")
     (let [facts (->> (o/query-all o/*session*)
                       (remove (fn [[id]]
                                 (or (= id ::t/derived)
                                     (= id ::todo)
                                     (= id ::passcode)
                                     (= id ::checkbox)))))]
       (println facts)
       (d/set-item! :facts facts))]})) 



