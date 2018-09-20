(ns util.task
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [rum.core :as rum]
            [components.router :refer [state set-page! current-page]]
            [me.raynes.fs :as fs]
            [util.server-render :as server]))
            ;[cljss.ssr :as ssr]))



;; Gets HTML Template
(defn html-template [] (slurp "src/template.html"))


;; Gets a sequence of the routes
(def routes-list
  (->> (seq (.list (io/file "src/routes")))
       (map (fn [r] (str/replace r #"_" "-")))
       (map (fn [r] (str/replace r #".cljc" "")))))


;; Render HTML page
#_(defn render-content []
    (binding [ssr/*ssr-ctx* (atom {:styles {}})]
      (let [html (current-page)
            [html css] (ssr/render-css html)
            _ (println css)]

        (rum/render-html html))))
;
(defn render-content []
  (let [[html-str css-str] (server/render-html (current-page))]
    (do
      (spit "public/new.css" css-str :append true)
      (println (str "Rendered CSS from: " (current-page)))
      (println (str "Rendered STRING: "html-str))
      html-str)))

#_(defn render-content []
    (rum/render-html (current-page)))


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
