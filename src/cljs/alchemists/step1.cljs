(ns alchemists.step1
  (:require 
   [alchemists.data :refer [COLORS ALCHEMICALS INGREDIENTS STATE]]
   [alchemists.utility :refer [bool? alchemical? detail? alchemical detail mix-into-potion
                               alchemical-to-image potion-to-image]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true :refer [div img span]]))

(defn view [{:keys [alchemical-x alchemical-y visible?] :as app}]
  (reify
    om/IRender
    (render [_]
      (div
       #js {:className "guess-result card-panel orange lighten-5"}
       (div
        #js {:className "text"}
        (dom/p
         nil
         "My Young Apprentice. So you played your first game of Alchemists and were embarassed. How is it that everyone else was able to make Sherlock level deductions and you are sitting here cautiously coloring in boxes like a six year old? Is there a trick to it? Are they cheating? Is it actual magic? Perhaps you are just stupid?")
        (dom/p nil "Stop.")
        (dom/p nil "Don't Panic.")
        (dom/p nil "After some practice (which other people probably aquired doing
   Sudoku) you will be able to deduce with the best of them. Once you have figured
   out the deductive aspect of the game, you will be free to concentrate on the
   actual game behind the game.")
        (dom/p
         nil
         "You are learning to recognize what potion will result when two alchemicals are mixed. "
         (span
          #js {:className "bold"}
          "To figure out what potion is created, look for a match in sign 
    and color between a big circle on one alchemical and a little circle on the other. 
    This is the resulting color and sign of the potion. If this match cannot be found, 
    then the potion is a neutral potion. ")
         "Practice this a few times below, reset the question with
   the refresh button."))
       (div
        #js {:className "refresh cursor unselectable"}
        (dom/i #js {:className "material-icons large"
                    :onClick #(let [x (rand-nth (vec ALCHEMICALS))
                                    y (rand-nth (vec (disj ALCHEMICALS x)))]
                                (om/update! app {:alchemical-x x :alchemical-y y}))}
               "refresh"))
       (div
        #js {:className "equation"}
        (img #js {:className "alchemical" :src (alchemical-to-image alchemical-x)})
        (dom/i #js {:className "material-icons large"} "add")
        (img #js {:className "alchemical" :src (alchemical-to-image alchemical-y)})
        (dom/i #js {:className "material-icons large"} "arrow_forward")
        (div
         #js {:className "result unselectable"}
         (if visible?
           (img #js {:className "potion"
                     :src (potion-to-image (mix-into-potion alchemical-x alchemical-y))})
           (div #js {:className "reveal cursor grow"
                     :onClick #(om/update! app [:visible?] true)}
                (dom/i #js {:className "material-icons large"} "help")
                "Click to Reveal"))))))))
