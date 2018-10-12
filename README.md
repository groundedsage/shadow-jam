# Shadow JAM

### Starter (Not Production Ready)

A JAMstack build tool for developing PWA's with Clojurescript. This build tool is focused on capturing Better Web Practices.

* Better Performance
* Better Accessibility
* Better Developer Experience
* Better Project Completion Times

Demo at https://shadow-jam-starter.netlify.com/

Bootstrapped using the [Browser Quickstart](https://github.com/shadow-cljs/quickstart-browser.git) for Shadow CLJS

Inspiration & Prior art:
GatsbyJS, Next, Preact-cli, SapperJS

## Goals

* Follow the [PRPL](https://developers.google.com/web/fundamentals/performance/prpl-pattern/) pattern
* 100/100 on Lighthouse audits out of the box
* Work well as a JAMstack template on Netlify

## Required Software

* [node.js (v10.3.0+)](https://nodejs.org/en/download/)
* [Java JDK (10+)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or [Open JDK (10+)](http://jdk.java.net/10/)

## Running the Build Tool

Clone the repository. Then `cd` into it.

```bash
npm install
shadow-cljs server
```

This runs the `shadow-cljs` server process which all following commands will talk to. Just leave it running and open a new terminal to continue.

The first startup takes a bit of time since it has to download all the dependencies and do some prep work. Once this is running we can get started.

```txt
shadow-cljs run jam/watch
```

This will begin the compilation of the configured `:app` build and re-compile whenever you change a file.

To do a production release. Stop the `watch` process. Set up your service worker by running.

```txt
npx workbox wizard --injectManifest
```

Then run.

```txt
shadow-cljs run jam/release
```

Now the same url will have a production release that can be used for testing locally eg. Chrome Lighthouse Audits.

# Why PWA's?

Progressive Web Apps are installable and live on the user's home screen, without the need for an app store
https://developers.google.com/web/progressive-web-apps/

The browser can do a lot. It has a high level of device integration (https://whatwebcando.today/) and with Web Assembly (reportedly running at 1.2x native code execution) it will be able to do a lot more in the future.

They are also installable on Desktop with Chrome right now. https://developers.google.com/web/updates/2018/05/dpwa

The following images are from Google IO talks and illustrates the size differences of a native application to a web application.
![twitter-pwa](https://user-images.githubusercontent.com/11351767/42217309-f9bda62c-7f07-11e8-801c-97e819ccc29e.png)
![pinterest-pwa](https://user-images.githubusercontent.com/11351767/42216431-207ced0c-7f05-11e8-93cc-60f843288477.png)

## Resources

### Performance

* Front-End Performance Checklist - https://github.com/thedaviddias/Front-End-Performance-Checklist#---------front-end-performance-checklist-

* Fast By Default: Modern Loading Best Practices (Chrome Dev Summit 2017) - https://www.youtube.com/watch?v=_srJ7eHS3IM

* Web performance made easy (Google I/O '18) - https://www.youtube.com/watch?v=Mv-l3-tJgGk&list=PLOU2XLYxmsIInFRc3M44HUTQc3b_YJ4-Y&index=46

* Speed Matters: Designing for Mobile Performance - http://www.awwwards.org/brainfood-mobile-performance-vol3.pdf

* Smashing Magazine Performance Checklist 2018 - https://www.smashingmagazine.com/2018/01/front-end-performance-checklist-2018-pdf-pages/

  * SpeedIndex < 1250
  * TTI < 5s on 3G
  * Critical file size budget < 170Kb.
  * The first 14~15Kb of the HTML is the most critical payload chunk

### Typography

* A great write up on why font sizes should be bigger - https://blog.marvelapp.com/body-text-small/

* Variable fonts allows multiple font style variations with a single font file - https://developers.google.com/web/fundamentals/design-and-ux/typography/variable-fonts/

* More people change their font size in the browser than use edge - https://medium.com/@vamptvo/pixels-vs-ems-users-do-change-font-size-5cfb20831773

* How to set up a System font stack so you can leverage the fonts already available on the system - https://woorkup.com/system-font/

  * system-ui value for font-family has 84.06% global compatibility

  * Lean towards super font families when using Google fonts etc to ensure text can be read in the maximum number of languages

### Accessibility

Disabled Javascript

* https://www.smashingmagazine.com/2018/05/using-the-web-with-javascript-turned-off/

* https://blockmetry.com/blog/javascript-disabled

## Interesting developments with React

* Using React Suspense to reduce placeholders and **remove ReactDOM** from bundle - https://www.youtube.com/watch?time_continue=851&v=z-6JC0_cOns
* Using Prepack to generate HTML: **React and ReactDOMServer are completely compiled away** - https://github.com/trueadm/ssr-with-prepack-hackathon

## TODO

* ✅ Pre-render HTML
* ✅ Inline critical CSS
* 🛠 Optionally subset Google Fonts (possible to subset local fonts with more dependencies)
  * ✅ Works for index HTML on release when true in config
* ✅ Use lazysizes for lazy loading images
* ✅ Local image manipulation using [Sharp](https://github.com/lovell/sharp) in a similar way to [gatsby-transformer-sharp](https://image-processing.gatsbyjs.org/)
  * Optionally inline if under 10kb as Data URI Gatsby does
* 🛠 Generate Service Workers using Workbox
* Setup HTTPS for Shadow-cljs
* Use [Hicada](https://github.com/rauhs/hicada) instead of Sablono to provide new features
* Optionally run Lighthouse audits on public pages locally
* Option to create static websites that have either no JS, minimal JS or the React Runtime
* Automatic code splitting for routes
* Assess Guess JS integration
* Assess DataURI optimisations for SVG with mini-svg-data-uri
* Process images using [SQIP](https://github.com/technopagan/sqip) to generate SVG's in a similar way to [Gatsby SQIP plugin](https://www.gatsbyjs.org/packages/gatsby-transformer-sqip/?=sqip).
  * Use 'svgo' to optimise the SVG if necessary
  * Use 'mini-svg-data-uri' on static images above the fold and inline into HTML.
