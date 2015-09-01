(ns alchemists.utility-macros)

(defmacro fn-prevent [[e] & body]
  `(fn [~e]
    ~@body
    (.preventDefault ~e)))

(defmacro defn-prevent [name [e] & body]
  `(defn ~name [~e]
     ~@body
     (.preventDefault ~e)))
