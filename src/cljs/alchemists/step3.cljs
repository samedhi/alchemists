(ns alchemists.step3
  (:require
   [alchemists.data :refer [COLORS ALCHEMICALS INGREDIENTS STATE]]
   [alchemists.utility :refer [bool? alchemical? detail? alchemical detail mix-into-potion
                               alchemical-to-image potion-to-image p]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   clojure.string))

(def deductive-game-text 
  (dom/div 
   #js {:className "text"}
   (dom/h3 nil "Step 3: Practice it")
   (p "Now that you know how to play the deductive aspect of this game, all you need"
      "is practice.")
   (p "Just like the real game, the following demo will let you observe the result"
      "of mixing two ingredients to get a result. Based on the resulting potion, you"
      "should be able to disable some of the alchemicals as possible alchemicals"
      "for those two ingredients. Repeat the process of randomly mixing two ingredients"
      "until you narrow some ingredient down to one alchemical. When this happens, the"
      "system will display that alchemical as the alchemical for that ingredient. Solve"
      "for all ingredients. That is the game.")
   (p "Good gaming and good luck.")))

(defn view [app owner]
  (reify 
    om/IRender
    (render [_]
      (dom/div 
       #js {:className "guess-alchemicals card-panel orange lighten-5"}
       (dom/div #js {:className "text"} deductive-game-text)))))

