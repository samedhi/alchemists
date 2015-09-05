(ns alchemists.step2
  (:require
   [alchemists.data :refer [COLORS ALCHEMICALS INGREDIENTS STATE]]
   [alchemists.utility :refer [bool? alchemical? detail? alchemical detail mix-into-potion
                               alchemical-to-image potion-to-image]]
   [clojure.set :refer [difference subset? superset?]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true :refer [div img span]]))

(def guess-alchemicals-text
  (div
   #js {:className "text"}
   (dom/p
    nil
    "Excellent, now that you have mastered it forward, lets do it in reverse!")
   (dom/p
    nil
    "In the game of Alchemists you are not given two"
    (span #js {:className "bold"} " known ")
    "alchemicals and asked for the resulting potion. You are given two"
    (span #js {:className "bold"} " unknown ")
    "alchemicals and told what potion resulted when you mixed them. The deductive part
     of the game is figuring out what the unknown alchemicals are (from a list of 8
     possibilities).")
   (dom/p
    nil
    "You can see what the resulting potion was when you mixed unkown 
    alchemical #1 with unkown alchemical #2. The 8 alchemicals are listed below.
    Using the rules you learned previously, what are the only alchemicals
    that could result in this potion? The alchemicals that #1 and #2 could be should be
    in color and the alchemicals that they are not should be black and white.")))

(defn view [{:keys [result visible? alchemicals] :as app}]
  (reify
    om/IRender
    (render [_]
      (div
       #js {:className "guess-alchemicals card-panel orange lighten-5"}
       (div #js {:className "text"} guess-alchemicals-text)
       (div
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
       (div
        #js {:className "equation"}
        (div #js {:className "reveal"}
                 (dom/i #js {:className "material-icons large"} "help")
                 "Alchemical #1")
        (dom/i #js {:className "material-icons large"} "add")
        (div #js {:className "reveal"}
                 (dom/i #js {:className "material-icons large"} "help")
                 "Alchemical #2")
        (dom/i #js {:className "material-icons large"} "arrow_forward")
        (div
         #js {:className "result unselectable"}
         (img #js {:className "potion" :src (potion-to-image result)})))
       (div #js {:className "query"} "Disable the alchemicals that #1 and #2 cannot be.")
       (apply
        div
        #js {:className "choices"}
        (map
         #(apply div #js {:className "row"} %)
         (partition
          4
          (for [a ALCHEMICALS]
            (img #js {:className (str "alchemical cursor"
                                          (when-not (contains? alchemicals a) " disabled"))
                          :src (alchemical-to-image a)
                          :onClick (fn [_] (om/transact!
                                            app
                                            [:alchemicals]
                                            #((if (contains? % a) disj conj) % a)))})))))
       (div
        #js {:className "feedback"}
        (let [fx (fn [a] (map #(set [a %]) (disj ALCHEMICALS a)))
              p (set (mapcat fx ALCHEMICALS))
              fp (remove #(let [[a b] (seq %)] (not= (mix-into-potion a b) result)) p)
              expected (set (mapcat seq fp))
              actual alchemicals]
          (cond
            (= actual expected)
            (div
             #js {:className "success"}
             (dom/i #js {:className "material-icons large"} "check")
             "Correct! You marked all the disabled alchemicals!")
            (superset? actual expected)
            (div
             #js {:className "everything-ok"}
             (dom/i #js {:className "material-icons large"} "check")
             "Disable the invalid alchemicals above.")
            (-> (difference expected actual) count pos?)
            (div
             #js {:className "error"}
             (dom/i #js {:className "material-icons large"} "clear")
             "Oops! You have disabled a valid alchemical!"))))))))
