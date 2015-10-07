(ns alchemists.step3
  (:require
   [alchemists.data :refer [STATE COLORS ALCHEMICALS INGREDIENTS STATE]]
   [alchemists.utility :refer [bool? alchemical? detail? alchemical detail mix-into-potion
                               alchemical-to-image potion-to-image p ingredient-to-image]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   clojure.string))

;; FX

(defn random-initial-state []
  {:expected (zipmap (shuffle INGREDIENTS) (shuffle ALCHEMICALS))})

;; STATE

(swap! STATE assoc :step3-data (random-initial-state))

;; HTML

(def deductive-game-text
  (dom/div
   #js {:className "text"}
   (dom/h3 nil "Step 3: Practice it")
   (p "Now that you know how to play the deductive aspect of this game, all you need"
      "is practice.")
   (p "Just like the real game, the following experiment will let you observe the result"
      "of mixing two ingredients to get a result. Based on the resulting potion, you"
      "should be able to disable some of the alchemicals as possible alchemicals"
      "for those two ingredients. Repeat the process of randomly mixing two ingredients"
      "until you narrow some ingredient down to one alchemical. When this happens, the"
      "system will display that alchemical as the alchemical for that ingredient. Solve"
      "for all ingredients. That is the game.")
   (p "Good gaming and good luck.")))

(defn build-pyramid [xs]
  (->> (map #(vector % %) xs)
       (iterate #(map (fn [[a _] [_ b]] [a b]) % (rest %)))
       (take-while (complement empty?))))

(defn pyramid-view [{:keys [expected] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (apply
       dom/div
       #js {:className "pyramid"}
       (for [row (-> expected build-pyramid reverse butlast)]
         (apply
          dom/div
          #js {:className "pyramid-row"}
          (for [[[_ a] [_ b]] row
                :let [m (mix-into-potion a b)
                      p (potion-to-image m)]]
            (dom/div 
             #js {:className "mixture"} 
             (dom/img #js {:className "potion" :src p})))))))))

(defn table-view [{:keys [expected] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (apply
       dom/div
       #js {:className "table"}
       (for [e expected]
         (dom/div
          #js {:className "column"}
          (dom/img
           #js {:className "ingredient"
                :src (->> e first ingredient-to-image)})))))))

(defn view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "guess-alchemicals card-panel orange lighten-5"}
       (dom/div #js {:className "text"} deductive-game-text)
       (dom/div
        #js {:className "refresh cursor unselectable"}
        (dom/i #js {:className "material-icons large"
                    :onClick #(om/update! app (random-initial-state))}
               "refresh"))
       (dom/div
        #js {:className "center-flex"}
        (om/build pyramid-view app)
        (om/build table-view app))))))
