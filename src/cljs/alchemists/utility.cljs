(ns alchemists.utility
  (:require
   [alchemists.data :refer [COLORS]]
   [clojure.string :refer [join]]))

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
