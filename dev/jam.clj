(ns jam
  (:require [shadow.cljs.devtools.api :as shadow]
            [util.task :refer :all]))


(defn release []
  ;; Configuration for release
  ;(when (true? (:subfont config))
  ;  (do
  ;    (println "🛠   Subsetting fonts ...")
  ;    (subfont-setter)
  ;    (println "✅  Finished font subsetting")
  ;    build-state
  ;(when (true? (:workbox config))
  ;  (do
  ;    (println "🛠   Injecting workbox manifest ...")
  ;    (workbox-manifest)
  ;    (println "✅  Finished injecting workbox manifest")
  (do
    (println "🛠   Rendering routes to HTML ...")
    (render-pages)
    (css-move)
    (println "🔗  Moved CSS to public folder")
    (manifest-move)
    (println "🔗  Moved Manifest.json to public folder")
    (println "🛠   Inlining css ...")
    (critical-css)
    (println "✅  Finished inlining css")
    (shadow/release :app)))

(defn watch
  {:shadow/requires-server true}
  []
  (do
    (println "🛠   Rendering routes to HTML ...")
    (render-index)
    (println "🔗  Movied CSS to public folder")
    (css-move)
    (shadow/watch :app)))
