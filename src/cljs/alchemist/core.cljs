(ns alchemist.core
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan dropping-buffer timeout close!]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   goog.dom
   goog.style)
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [alchemist.utility-macros :refer [defn-prevent fn-prevent]]))

(enable-console-print!)

(def STATE
  (atom {}))

(defn view [{:keys [modal-view thread-view?] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "root"}
       (dom/img #js {:className "ingredient" :src "image/ivy.jpg"})
       (dom/img #js {:className "ingredient" :src "image/chicken-foot.jpg"})
       (dom/img #js {:className "ingredient" :src "image/mushroom.jpg"})
       (dom/img #js {:className "ingredient" :src "image/forget-me-not.jpg"})
       (dom/img #js {:className "ingredient" :src "image/mandrake.jpg"})
       (dom/img #js {:className "ingredient" :src "image/scorpion.jpg"})
       (dom/img #js {:className "ingredient" :src "image/toad.jpg"})
       (dom/img #js {:className "ingredient" :src "image/crow-feather.jpg"})))))

(om/root
 view
 STATE
 {:target (goog.dom.getElement "app")})

(add-watch STATE :state-change (fn [_ _ _ n] (println (dissoc n :groups))))
