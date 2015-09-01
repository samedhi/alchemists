(ns alchemists.core
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan dropping-buffer timeout close!]]
   [clojure.string :refer [join]]
   [clojure.set :refer [difference subset? superset?]]
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
                   :visible? false}
                  :guess-alchemicals
                  {:result {:color :blue
                            :positive? false}
                   :alchemicals ALCHEMICALS}}))

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

(def guess-results-text
  (dom/div
   #js {:className "text"}
   (dom/p
    nil
    "My Young Apprentice. So you played your first game of Alchemists and were embarassed? How is it
   that everyone else was able to make Sherlock level deductions
   and you are sitting here cautiously coloring in boxes like a six year old? Is 
   there a trick to it? Are they cheating? Is it actual magic? Perhaps you are just stupid?")
   (dom/p nil "Stop.")
   (dom/p nil "Don't Panic.")
   (dom/p nil "After some practice (which other people probably aquired doing
   Sudoku) you will be able to deduce with the best of them. Once you have figured
   out the deductive aspect of the game, you will be free to concentrate on the
   actual game behind the game.")
   (dom/p
    nil
    "You are learning to recognize what potion will result when two alchemicals are mixed. "
    (dom/span
     #js {:className "bold"}
     "To figure out what potion is created, look for a match in sign 
    and color between a big circle on one alchemical and a little circle on the other. 
    This is the resulting color and sign of the potion. If this match cannot be found, 
    then the potion is a neutral potion. ")
    "Practice this a few times below, reset the question with
   the refresh button.")))

(defn guess-result-view [{:keys [alchemical-x alchemical-y visible?] :as app}]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "guess-result card-panel orange lighten-5"}
       guess-results-text
       (dom/div
        #js {:className "refresh cursor unselectable"}
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
           (dom/div #js {:className "reveal cursor grow"
                         :onClick #(om/update! app [:visible?] true)}
                    (dom/i #js {:className "material-icons large"} "help")
                    "Click to Reveal"))))))))

(def guess-alchemicals-text
  (dom/div
   #js {:className "text"}
   (dom/p
    nil
    "Excellent, now that you have mastered it forward, lets do it in reverse!")
   (dom/p
    nil
    "You see, in the game of Alchemists you are not
    given the alchemicals and asked for the resulting potion. You are given two"
    (dom/span #js {:className "bold"} " unknown ")
    "alchemicals and told what potion resulted when you mixed them. The deductive part
     of the game is figuring out what the unkown alchemicals are (from a list of 8
     possiblities).")
   (dom/p
    nil
    "Practice this below. You can see what the resulting potion was when you mixed unkown 
    alchemical #1 with unkown alchemical #2. The 8 alchemicals are listed below.
    Using the rules you learned previously, what are the only alchemicals
    that could result in this potion? The alchemicals that #1 and #2 could be should be
    in color and the alchemicals that they are not should be black and white.")))

(defn guess-alchemicals-view [{:keys [result visible? alchemicals] :as app}]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "guess-alchemicals card-panel orange lighten-5"}
       (dom/div #js {:className "text"} guess-alchemicals-text)
       (dom/div
        #js {:className "refresh cursor unselectable"}
        (dom/i #js {:className "material-icons large"
                    :onClick #(om/update!
                               app
                               {:result
                                (if (zero? (rand-int 8))
                                  {:color :neutral}
                                  {:color (rand-nth COLORS) :positive? (rand-nth [true false])})
                                :alchemicals ALCHEMICALS})}
              "refresh"))
       (dom/div
        #js {:className "equation"}
        (dom/div #js {:className "reveal"}
                 (dom/i #js {:className "material-icons large"} "help")
                 "Alchemical #1")
        (dom/i #js {:className "material-icons large"} "add")
        (dom/div #js {:className "reveal"}
                 (dom/i #js {:className "material-icons large"} "help")
                 "Alchemical #2")
        (dom/i #js {:className "material-icons large"} "arrow_forward")
        (dom/div
         #js {:className "result unselectable"}
         (dom/img #js {:className "potion" :src (potion-to-image result)})))
       (dom/div #js {:className "query"} "Disable the alchemicals that #1 and #2 cannot be.")
       (apply
        dom/div
        #js {:className "choices"}
        (map
         #(apply dom/div #js {:className "row"} %)
         (partition
          4
          (for [a ALCHEMICALS]
            (dom/img #js {:className (str "alchemical cursor"
                                          (when-not (contains? alchemicals a) " disabled"))
                          :src (alchemical-to-image a)
                          :onClick (fn [_] (om/transact!
                                            app
                                            [:alchemicals]
                                            #((if (contains? % a) disj conj) % a)))})))))
       (dom/div
        #js {:className "feedback"}
        (let [fx (fn [a] (map #(set [a %]) (disj ALCHEMICALS a)))
              p (set (mapcat fx ALCHEMICALS))
              fp (remove #(let [[a b] (seq %)] (not= (mix-into-potion a b) result)) p)
              expected (set (mapcat seq fp))
              actual alchemicals]
          (cond
            (= actual expected)
            (dom/div
             #js {:className "success"}
             (dom/i #js {:className "material-icons large"} "check")
             "Correct! You marked all the disabled alchemicals!")
            (superset? actual expected)
            (dom/div
             #js {:className "everything-ok"}
             (dom/i #js {:className "material-icons large"} "check")
             "Disable the invalid alchemicals above.")
            (-> (difference expected actual) count pos?)
            (dom/div
             #js {:className "error"}
             (dom/i #js {:className "material-icons large"} "clear")
             "Oops! You have disabled a valid alchemical!"))))))))

(defn view [{:keys [guess-result guess-alchemicals] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
       #js {:className "root container"}
       (dom/img #js {:className (str "banner") :src "image/alchemists.png"})
       (dom/div
        #js {:className "card-panel orange lighten-5"}
        (dom/h2 #js {:style #js {:textAlign "center"}} "Alchemists")
        (dom/h5 #js {:style #js {:textAlign "center"}}
                "Or, how I stopped worrying about deduction and enjoyed the game.")
        (dom/div
         #js {:className "links"}
         (dom/a #js {:href "http://czechgames.com/en/alchemists/"} "Alchemist site")
         (dom/a #js {:href "http://czechgames.com/en/alchemists/downloads/"} "Alchemist rules")
         (dom/a #js {:href "https://github.com/samedhi/alchemist"} "Github contributions")))
       (om/build guess-result-view guess-result)
       (om/build guess-alchemicals-view guess-alchemicals)))))

(om/root
 view
 STATE
 {:target (goog.dom.getElement "app")})

(add-watch STATE :state-change (fn [_ _ o n] (when (not= o n) (println n))))
