(ns components.image
  (:require [clojure.string :as str :refer [includes?]]
            [clojure.data.json :as json]
            [me.raynes.fs :as fs]))



;; Decision and Research Documentation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ## Client Hints is a great option but it is not a well supported option at both the browser level and CDN level.
;; https://developers.google.com/web/updates/2015/09/automating-resource-selection-with-client-hints

;; ## Doing Compression
;; Dynamic image compression https://github.com/rflynn/imgmin#quality-details
;; Optimize PNG with https://github.com/kornelski/pngquant

;; ## Perceived performance and UX Research on Image placeholders
;; https://blog.radware.com/applicationdelivery/wpo/2014/09/progressive-image-rendering-good-evil/
;; https://www.smashingmagazine.com/2018/02/progressive-image-loading-user-perceived-performance/
;; Note from Smashing Mag: - Structural Similarity Index is a great way to predict percieved quality


;; ## Placeholder options:
;; 1. Transparent Gif as Data URI
;; 2. SQIP - SVG placeholder that can closely resemble the final image
;; 3. Traced SVG
;; 4. Single colour background
;; 5. None - block until resource is loaded using the new React Suspense functionality

;; (1.) Its important to note that placeholders can have an effect on on SEO, Social Media sharing etc.
;; Modern blank gif approach which resolves this https://ivopetkov.com/b/lazy-load-responsive-images/

;; (2.) SQIP is a decent placeholder over LQIP. Higher fidelity due to scaling to any screensize or resolution and allows creative customisability.
;; https://github.com/technopagan/sqip
;; https://axe312ger.github.io/embedded-svg-loading-impact-research/index.html
;; https://github.com/gatsbyjs/gatsby/tree/master/packages/gatsby-transformer-sqip
;; Gatsby recommendations
;;    - Smaller thumbnails should range between 500-1000byte
;;    - A single header image or a full sized hero might take 1-10kb
;;    - For frequent previews like article teasers or image gallery thumbnails I’d recommend 15-25 shapes
;;    - For header and hero images you may go up to 50-200 shapes-

;; (3.) Traced svg provides a cool effect when doing fade out and fade in and using colour
;; Demo - https://portfolio-emilia.netlify.com/
;; Code - https://github.com/LeKoArts/gatsby-starter-portfolio-emilia/blob/689e50dac5e39b4a5f87dbf4408a986438c526d2/src/pages/index.js#L69



;; Constants

(def valid-formats {:webp "webp" :png "png" :jpg "jpg"})

(def transparent-gif "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==")


;; Auxillery functions

(defn sizes
  "Number -> Vector ->  Vector
   Multiplies dimension [d] by the multipliers in vector [v] and returns a vector of dimensions."
  [d v]
  (vec (map #(int (+ 0.5 (* % d))) v)))


(defn fluid-sizes [{max-width :max-width max-height :max-height}]
  (sizes max-width [0.25 0.5 1 1.5 2 3]))


(defn resolutions [{width :width height :height}]
  (sizes width [1 1.5 2 3]))


(defn format?
  "String -> Keyword
   Takes the url of the image [src] and returns the just the format as a keyword"
  [src]
  (cond
    (includes? src ".webp") :webp
    (includes? src ".png") :png
    (or (includes? src ".jpg") (includes? src ".jpeg")) :jpg
    (or (includes? src ".tif") (includes? src ".tiff")) :tiff))


(defn src-set
  "String -> String -> Vector -> Keyword ->  String
   Takes the image url [src] without extension, the image [format], the new image widths [dimensions] and the responsive [type]. Returns a string listing the src-set"
  [src format dimensions type]
  (->>
    (case type
         :sizes (map #(str src "-" % "." format " " % "w") dimensions)
         :resolutions (map #(str src "-" % "." format " " %2 "x") dimensions [1 1.5 2 3]))
    (str/join ", ")))


(defn remove-extension
  "String -> String
   Removes the format extension from the url of the image [src]"
  [src]
  (let [extensions [".webp" ".jpg" ".jpeg" ".png"]
        pattern (->> extensions
                     (map #(java.util.regex.Pattern/quote %))
                     (interpose \|)
                     (apply str))]
    (.replaceAll src pattern "")))


(defn get-image-dimensions
  "String -> Map
   Takes the url of an image [src] and returns a map with the dimensions"
  [src]
  (with-open [r (java.io.FileInputStream. src)]
        (let [image (javax.imageio.ImageIO/read r)]
           {:width (.getWidth image) :height (.getHeight image)})))


(defn calc-aspect-ratio
  [width height]
  (->>
    (/ height width)
    (float)
    (* 100)
    (format "%.2f")
    (Double.)))


(defn get-aspect-ratio
  "String -> Float
   Takes the url of an image [src] and returns the aspect ratio as a float rounded to two decimals"
  [src]
  (let [dimensions (get-image-dimensions (str "src/" src))
        width (:width dimensions)
        height (:height dimensions)]
    (calc-aspect-ratio width height)))


(defn call-sharp-lib! [src src-without-extension format type dimensions]
  (println "🛠   Performing image manipulation on: " src-without-extension)
  (clojure.java.shell/sh "node" "dev/scripts/sharp-tool"
    (let [widths (if (= :sizes type) (fluid-sizes dimensions) (resolutions dimensions))]
      (json/write-str {:formats {:webp true
                                 :jpg (= :jpg format)
                                 :png (= :png format)}
                       :widths widths
                       :src (str "src/" src)
                       :srcWithoutExtension src-without-extension}))))


;; Auxillery functions directly related to final datastructure output

(defn source [src format dimensions type]
  (let [format (format valid-formats)]
    (case type
      :sizes  [:source {:type (str "image/" format)
                        :src-set transparent-gif
                        :data-srcset (str/join ", " (map #(str src "/" % "." format " " % "w") (fluid-sizes dimensions)))}]
      :resolutions  [:source {:type (str "image/" format)
                              :src-set transparent-gif
                              :data-srcset (str/join ", " (map #(str src "/" % "." format " " %2 "x") (resolutions dimensions) [1 1.5 2 3]))}])))
;
(defonce images-list
    (->> (fs/iterate-dir "public/images")
         (reduce #(conj %1 {(fs/base-name (first %2)) (nth %2 2)}) {})))



;; Main Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn image  [{src :src
               alt :alt
               responsive :responsive}]
  (do
    (when (empty? alt) (println (str  "⛔  ACCESIBILITY WARNING: " src " is used and missing an alt description")))
    (let [src-without-extension (remove-extension src)
          format (format? src)
          aspect-ratio (str (get-aspect-ratio src) "%")
          responsive-type (first (keys responsive))
          dimensions (responsive-type responsive)]
      (do
        ;; Still needs to perform a thorough check of what files have been generated and do a sweeping clean of what is not recorded as necessary
        (if (get images-list (str/replace src-without-extension #"images/" ""))
          nil
          (do
            (fs/mkdir (str "public/" src-without-extension))
            (call-sharp-lib! src src-without-extension format responsive-type dimensions)))



        ;; Transparent placeholder
        [:div {:style dimensions}
         [:div.fade-box
          {:style
            {:padding-bottom aspect-ratio
                          :height "0"
                          :position "relative"}}
          [:picture
           (source src-without-extension :webp dimensions responsive-type)
           (source src-without-extension format dimensions responsive-type)
           [:img.lazyload {:src (str src-without-extension "." (format valid-formats))
                           :alt alt
                           :data-sizes "auto"
                           :style {:width "100%"
                                   :position "absolute"
                                   :top 0
                                   :left 0
                                   :height "100%"}}]]]]))))


(defmacro inline-image [m]
  (image m))
