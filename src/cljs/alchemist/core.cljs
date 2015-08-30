(ns alchemist.core
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan dropping-buffer timeout close!]]
   [clojure.string :refer [join]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   goog.dom
   goog.style)
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

;; DATA

(def COLORS [:red :green :blue])

(def ALCHEMICALS #{:r-g+B- :r+g-B+ :r+G-b- :r-G+b+ :R-g-b+ :R+g+b- :R-G-B- :R+G+B+})

(def INGREDIENTS #{:ivy :chicken-foot :mushroom :forget-me-not
                   :mandrake :scorpion :toad :crow-feather})

(def STATE (atom {:guess-result
                  {:alchemical-x :R-G-B-
                   :alchemical-y :R+G+B+
                   :visible? false}}))

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

(defn mix-into-potion [x y]
  (let [x (if (detail? x) detail (detail x))
        y (if (detail? y) detail (detail y))
        potion-created? (fn [[_ b c]]
                          (and (= (:positive? b) (:positive? c))
                               (not= (:large? b) (:large? c))))
        [[r m _] :as rs] (->> (map #(vector %1 (get x %1) (get y %1)) COLORS)
                                (filter potion-created?))]
    (assert (-> rs count (<= 1)) "It should yield at most one potion (or a tasty soup)")
    (if (empty? rs)
      {:color :neutral}
      {:color r :positive? (:positive? m)})))

(defn alchemical-to-image [alchemical]
  (let [alchemical (detail alchemical)
        fx (fn [color]
             (let [{:keys [positive? large?]} (get alchemical color)]
               (str (-> color name first)
                    (if large? "l" "s")
                    (if positive? "p" "n"))))]
    (str "image/" (join "_" (map fx COLORS)) ".png")))

(defn potion-to-image [{:keys [positive? color]}]
  (str
   "image/"
   (join
      "_"
      (remove
       nil?
       ["potion"
        (name color)
        (cond positive? "positive" (false? positive?) "negative")]))
   ".png") )

;; OM


(defn guess-result-view [{:keys [alchemical-x alchemical-y visible?] :as app}]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "guess-result"}
       (dom/div
        #js {:className "refresh unselectable"}
        (dom/i #js {:className "material-icons large"
                   :onClick #(let [x (rand-nth (vec ALCHEMICALS))
                                   y (rand-nth (vec (disj ALCHEMICALS x)))]
                               (om/update! app {:alchemical-x x :alchemical-y y}))}
              "refresh"))
       (dom/div
        #js {:className "equation"}
        (dom/img #js {:className "alchemical" :src (alchemical-to-image alchemical-x)})
        (dom/i #js {:className "material-icons large"} "add")
        (dom/img #js {:className "alchemical" :src (alchemical-to-image alchemical-y)})
        (dom/i #js {:className "material-icons large"} "arrow_forward")
        (dom/div
         #js {:className "result unselectable"}
         (if visible?
           (dom/img #js {:className "potion"
                         :src (potion-to-image (mix-into-potion alchemical-x alchemical-y))})
           (dom/div #js {:className "reveal grow"
                         :onClick #(om/update! app [:visible?] true)}
                    (dom/i #js {:className "material-icons large"} "help")
                    "Click to Reveal"))))))))

(defn view [{:keys [guess-result] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "root container"}
       (om/build guess-result-view guess-result)))))

(om/root
 view
 STATE
 {:target (goog.dom.getElement "app")})

(add-watch STATE :state-change (fn [_ _ o n] (when (not= o n) (println n))))
