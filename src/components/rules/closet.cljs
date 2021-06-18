(ns components.rules.closet
  (:require [odoyle.rules :as o]))

(def closet-rules
  (o/ruleset
   {::check-passcode
    [:what
     [::global ::correct-passcode correct]
     [::global ::inserted-passcode inserted]
     :then
     (if (= correct (reduce str inserted))
       (o/insert! ::global ::valid-passcode? true)
       nil)]}))