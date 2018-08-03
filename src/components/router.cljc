(ns components.router
  (:require [rum.core :as rum]
            [routes.index :refer [home]]
            [routes.page-2 :refer [page-2]]


            [reitit.core :as r]
            #?(:cljs [pushy.core :as pushy])))

;; The App routes
(def app-routes
  (r/router
    [["/" :index]
     ["/index" :index]
     ["/page-2" :page-2]]))



;; Pushy library for HTML5 Pushstate
(def state (atom {}))


(defn set-page! [match]
  (swap! state assoc :page match))



#?(:cljs
    (def history
      (pushy/pushy set-page! (partial r/match-by-path app-routes))))
#?(:cljs
    (pushy/start! history))
#?(:cljs
    (defn set-token! [token] (set-token! history token)))

#?(:cljs
    (defn get-route-keyword [s] (:name (:data (:page s)))))



(rum/defc page-content < rum/reactive []
  (let [token #?(:cljs (get-route-keyword (rum/react state))
                 :clj  (:page (rum/react state)))]
    (case token
      :index  (home)
      :page-2 (page-2))))




(rum/defc current-page []
  [:div
   [:header
    [:h1 "Shadow JAM"]]
   (page-content)])
