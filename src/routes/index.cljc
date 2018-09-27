(ns routes.index
  (:require [rum.core :as rum]
            #?(:clj [components.image :refer [inline-image]]))
  #?(:cljs (:require-macros [components.image :refer [inline-image]]))
  #?(:cljs (:require [cljss.core :as css :refer-macros [defstyles defkeyframes]])))





(rum/defc home []
   [:main
    [:h1 {:css  {:background "green"
                 :animation-delay "0.15s"
                 :&:hover {:background "red"}
                 :&:active {:opacity "0.3"}
                 :&:focus {:background "yellow"}

                 ::css/media {[[:hover "none"] [:hover "on-demand"]]
                              {:&:hover {:background "green"}}}}}
                               ;:&:active {:opacity "0.3"}}}}}



      "Welcome to Shadow JAM"]
    [:span
      [:a {:href "/page-2"}

        "Page 2"]]
    (inline-image {:src "images/jam.jpeg"
                   :alt "Row of Jam"
                   :responsive {:sizes {:max-width 800}}})])
