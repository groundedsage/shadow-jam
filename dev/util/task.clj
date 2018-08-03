(ns util.task
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [rum.core :as rum]
            [components.router :refer [state set-page! current-page]]
            [me.raynes.fs :as fs]))



;; Gets HTML Template
(defn html-template [] (slurp "src/template.html"))


;; Gets a sequence of the routes
(def routes-list
  (->> (seq (.list (io/file "src/routes")))
       (map (fn [r] (str/replace r #"_" "-")))
       (map (fn [r] (str/replace r #".cljc" "")))))


;; Render HTML page
(defn render-content [] (rum/render-html (current-page)))


;; Render HTML page for :watch
(defn render-index []
  (do
    (set-page! :index)
    (spit "public/index.html"
      (->
        (str/replace (html-template) #"\{ ssr-html \}" "")
        (str/replace  #"\{ main-script \}" "<script src=\"/js/main.js\"></script><script>components.app.init();</script>")))))



;; SW.js registration script
(def service-worker-script
     "<script>
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js')
        .then(registration => {
          console.log('Service Worker registered! Scope: ' + registration.scope);
        })
        .catch(err => {
          console.log('Service Worker registration failed: ' + err);
        });
    });
  }
</script>")


;; Render HTML pages
(defn render-pages []
  (do
    (loop [routes routes-list]
      (if (empty? routes)
        (println "✅  Finished rendering HTML")
        (let [page (first routes)
              _ (set-page! (keyword page))]
          (do
            (spit
              (str "public/" page ".html")
              (->
                (str/replace (html-template) #"\{ ssr-html \}" (render-content))
                (str/replace  #"\{ main-script \}" "<script defer src=\"/js/main.js\" onload=\"components.app.init();\"></script>")
                (str/replace  #"\{ workbox-script \}" service-worker-script)))
            (recur (rest routes))))))))


;; Move CSS to public folder
(defn css-move []
  (spit "public/css/main.css" (slurp "src/main.css")))

(defn manifest-move []
  (spit "public/manifest.json" (slurp "src/manifest.json")))

;; Node scripts

(defn audit []
   (clojure.java.shell/sh "node" "dev/scripts/audit"))


(defn critical-css []
   (clojure.java.shell/sh "node" "dev/scripts/css-inliner"))


(defn subfont-setter []
   (clojure.java.shell/sh "subfont" "public/index.html" "--inline-css" "-i"))

(defn workbox-manifest []
  (clojure.java.shell/sh  "npx" "workbox" "injectManifest" "workbox-config.js"))


;; Build hooks

(defn clean
  {:shadow.build/stage :configure}
  [{:shadow.build/keys [mode] :as build-state}]
  (do
    (println "🛠   Cleaning image folder...")
    (fs/delete-dir "public/images")
    (fs/mkdir "public/images")
    (fs/copy-dir "src/images/static" "public/images/")
    (println "🔗  Moved Static images to public folder")
    (println "✅   Fished cleaning image folder")
    build-state))

(defn make-jam
  {:shadow.build/stage :flush}
  [{:shadow.build/keys [mode] :as build-state} config]
  (case mode
    :release (do
              (println "🛠   Rendering routes to HTML ...")
              (render-pages)
              (css-move)
              (println "🔗  Moved CSS to public folder")
              (manifest-move)
              (println "🔗  Moved Manifest.json to public folder")
              (println "🛠   Inlining css ...")
              (critical-css)
              (println "✅  Finished inlining css")
              (when (true? (:subfont config))
                (do
                  (println "🛠   Subsetting fonts ...")
                  (subfont-setter)
                  (println "✅  Finished font subsetting")
                  build-state))
              (when (true? (:workbox config))
                (do
                  (println "🛠   Injecting workbox manifest ...")
                  (workbox-manifest)
                  (println "✅  Finished injecting workbox manifest")
                  build-state))
              build-state)
    :dev     (do
              (println "🛠   Rendering routes to HTML ...")
              (render-index)
              (println "🔗  Movied CSS to public folder")
              (css-move)
              build-state)))
