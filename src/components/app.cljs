(ns components.app
  (:require [rum.core :as rum]
            [components.router :refer [current-page]]))



;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rum/hydrate (current-page)
             (. js/document (getElementById "app")))

  (js/console.log "start"))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
