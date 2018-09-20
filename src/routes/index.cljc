(ns routes.index
  (:require [rum.core :as rum]
            #?(:clj [components.image :refer [inline-image]]))
  #?(:cljs (:require-macros [components.image :refer [inline-image]]))
  #?(:cljs (:require-macros [cljss.core :as css]))
  #?(:cljs (:require [cljss.core :as css])))






(rum/defc home []
   [:main {:css {:background-color "blue"}}
    [:h1 {:css {:color "red"}} "Welcome to Shadow JAM."]
    [:span [:a {:href "/page-2"} "Page 2"]]
    (inline-image {:src "images/jam.jpeg"
                   :alt "Row of Jam"
                   :responsive {:sizes {:max-width 800}}})])
