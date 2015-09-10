(ns alchemists.core
  (:require
   [alchemists.data :as data]
   [alchemists.step1 :as step1]
   [alchemists.step2 :as step2]
   [alchemists.step3 :as step3]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   goog.dom
   goog.style))

(enable-console-print!)

(defn view [{:keys [step1-data step2-data step3-data] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "root container"}
       #_(dom/img #js {:className (str "banner") :src "image/alchemists.png"})
       #_(dom/div
        #js {:className "card-panel orange lighten-5"}
        (dom/h2 #js {:style #js {:textAlign "center"}} "Alchemists")
        (dom/h5 #js {:style #js {:textAlign "center"}}
                "Or, how I stopped worrying about deduction and enjoyed the game.")
        (dom/div
         #js {:className "links"}
         (dom/a #js {:href "http://czechgames.com/en/alchemists/"} "Alchemist site")
         (dom/a #js {:href "http://czechgames.com/en/alchemists/downloads/"} "Alchemist rules")
         (dom/a #js {:href "https://github.com/samedhi/alchemist"} "Github contributions")))
       ;; (om/build step1/view step1-data)
       ;; (om/build step2/view step2-data)
       (om/build step3/view step3-data)))))

(om/root
 view
 data/STATE
 {:target (goog.dom.getElement "app")})

(add-watch data/STATE :state-change (fn [_ _ o n] (when (not= o n) (println n))))
