(ns routes.page-2
  (:require [rum.core :as rum]))

(rum/defc page-2 []
   [:main
    [:h1 "Second Page"]
    [:span [:a {:href "/"} "Back to first page"]]])
  
