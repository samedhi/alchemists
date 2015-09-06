(ns alchemists.data)

(def COLORS [:red :green :blue])

(def ALCHEMICALS #{:r-g+B- :r+g-B+ :r+G-b- :r-G+b+ :R-g-b+ :R+g+b- :R-G-B- :R+G+B+})

(def INGREDIENTS #{:ivy 
                   :chicken-foot 
                   :mushroom 
                   :forget-me-not
                   :mandrake 
                   :scorpion 
                   :toad 
                   :crow-feather})

(def STATE (atom {}))
