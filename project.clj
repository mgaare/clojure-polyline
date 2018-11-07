(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/clojure-polyline VERSION
  :description "library to encode and decode google polyline algorithm"
  :url "http://github.com/jesims/clojure-polyline"
  :license {:name         "Eclipse Public License"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.5"]]
  :parent-project {:coords  [io.jesi/parent "0.0.2"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :exclusions [:profiles :dev]]}
  :dependencies [[org.clojure/clojure]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [thheller/shadow-cljs]]
                   :plugins      [[lein-shell "0.5.0"]]
                   :source-paths ["dev"]}}
  :aliases {"node-test" ["do"
                         "run" "-m" "shadow.cljs.devtools.cli" "release" "test,"
                         "shell" "node" "target/node/test.js"]})
