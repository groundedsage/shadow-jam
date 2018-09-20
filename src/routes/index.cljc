(ns routes.index
  (:require [rum.core :as rum]
            [cljss.core :as css]
            #?(:clj [components.image :refer [inline-image]]))
  #?(:cljs (:require-macros [components.image :refer [inline-image]])))







(rum/defc home []
   [:main {:css {:background-color "red"}}
    [:h1 {:css {:color "yellow"}} "Welcome to Shadow JAM."]
    [:span [:a {:href "/page-2"} "Page 2"]]
    (inline-image {:src "images/jam.jpeg"
                   :alt "Row of Jam"
                   :responsive {:sizes {:max-width 800}}})])
