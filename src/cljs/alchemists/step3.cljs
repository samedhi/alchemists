(ns alchemists.step3
  (:require
   [alchemists.data :refer [COLORS ALCHEMICALS INGREDIENTS STATE]]
   [alchemists.utility :refer [bool? alchemical? detail? alchemical detail mix-into-potion
                               alchemical-to-image potion-to-image]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true :refer [div img span]]))

(def deductive-game-text 
  (div 
   #js {:className "text"} 
  (dom/p
    nil 
    "You now know all there is to play the deductive aspect of this game. Now all"
    "you need do is practice.")))

(defn view [{:keys [] :as app} owner]
  (reify 
    om/IRender
    (render [_]
      (div 
       #js {:className "guess-alchemicals card-panel orange lighten-5"}
       (div #js {:className "text"} deductive-game-text)))))

