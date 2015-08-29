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

;; DATA

(def COLORS [:red :green :blue])

(def CHEMICALS #{:r-g+B- :r+g-B+ :r+G-b- :r-G+b+ :R-g-b+ :R+g+b- :R-G-B- :R+G+B+})

(def STATE (atom {}))

;; FUNCTIONS

(defn chemical? [chemical]
  (if (keyword? chemical)
    (let [c (name chemical)
          [m & _] (re-find #"(R|r)(\+|-)(G|g)(\+|-)(B|b)(\+|-)" c)]
      (and (keyword? chemical) (= m c)))
    false))

(defn detail? [detail]
  (and (every? (fn [k] (contains? detail k)) COLORS)
       (every?
        (fn [k]
          (let [s (get-in detail [k :large?])] (or (true? s) (false? s))))
        COLORS)
       (every?
        (fn [k]
          (let [s (get-in detail [k :positive?])] (or (true? s) (false? s))))
        COLORS)))

(defn chemicals [detail]
  {:pre [(detail? detail)]
   :post [(chemical? %)]}
  (keyword
   (str
    (if (-> detail :red   :large?) "R" "r")
    (if (-> detail :red   :large?) "+" "-")
    (if (-> detail :green :large?) "G" "g")
    (if (-> detail :green :large?) "+" "-")
    (if (-> detail :blue  :large?) "B" "b")
    (if (-> detail :blue  :large?) "+" "-"))))

(defn details [chemical]
  {:pre [(chemical? chemical)]
   :post [(detail? %)]}
  (apply
   merge
   (for [[c s p] (map cons [:red :green :blue] (partition 2 (name chemical)))
         :let [capitalized? (=  (.toUpperCase s) s)
               polarity (= "+" p)]]
     {c {:large? capitalized? :positive? polarity}})))

;; OM

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
