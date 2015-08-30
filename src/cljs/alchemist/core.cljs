(ns alchemist.core
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan dropping-buffer timeout close!]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   goog.dom
   goog.style)
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

;; DATA

(def COLORS [:red :green :blue])

(def AlCHEMICALS #{:r-g+B- :r+g-B+ :r+G-b- :r-G+b+ :R-g-b+ :R+g+b- :R-G-B- :R+G+B+})

(def INGREDIENTS #{:ivy :chicken-foot :mushroom :forget-me-not
                   :mandrake :scorpion :toad :crow-feather})

(def STATE (atom {}))

;; FUNCTIONS

(defn bool? [b]
  (or (true? b) (false? b)))

(defn alchemical? [alchemical]
  (if (keyword? alchemical)
    (let [c (name alchemical)
          [m & _] (re-find #"(R|r)(\+|-)(G|g)(\+|-)(B|b)(\+|-)" c)]
      (= m c))
    false))

(defn detail? [detail]
  (and (every? (fn [k] (contains? detail k)) COLORS)
       (every? #(bool? (get-in detail [% :large?])) COLORS)
       (every? #(bool? (get-in detail [% :positive?])) COLORS)))

(defn alchemical [detail]
  {:pre [(detail? detail)]
   :post [(alchemical? %)]}
  (keyword
   (str
    (if (-> detail :red   :large?) "R" "r")
    (if (-> detail :red   :positive?) "+" "-")
    (if (-> detail :green :large?) "G" "g")
    (if (-> detail :green :positive?) "+" "-")
    (if (-> detail :blue  :large?) "B" "b")
    (if (-> detail :blue  :positive?) "+" "-"))))

(defn detail [chemical]
  {:pre [(alchemical? chemical)]
   :post [(detail? %)]}
  (apply
   merge
   (for [[c s p] (map cons [:red :green :blue] (partition 2 (name chemical)))
         :let [capitalized? (=  (.toUpperCase s) s)
               polarity (= "+" p)]]
     {c {:large? capitalized? :positive? polarity}})))

(defn mix [x y]
  (let [x (if (detail? x) detail (detail x))
        y (if (detail? y) detail (detail y))
        potion-created? (fn [[_ b c]]
                          (and (= (:positive? b) (:positive? c))
                               (not= (:large? b) (:large? c))))
        rs (->> (map #(vector %1 (get x %1) (get y %1)) COLORS)
                (filter potion-created?)
                (map first))]
    (assert (-> rs count (<= 1)) "It should yield at most one potion (or a tasty soup)")
    (if (empty? rs)
      :neutral
      (first rs))))

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
