(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/clojure-polyline VERSION
  :description "library to encode and decode google polyline algorithm"
  :url "http://github.com/jesims/clojure-polyline"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "1.10.339" :exclusions [com.google.javascript/closure-compiler-unshaded]]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [criterium "0.4.4"]
                                  [thheller/shadow-cljs "2.6.22"]]
                   :plugins      [[lein-shell "0.5.0"]]
                   :source-paths ["dev"]}}
  :aliases {"node-test" ["do" "run" "-m" "shadow.cljs.devtools.cli" "release" "test," "shell" "node" "target/node/test.js"]}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password
                                    :sign-releases false}]])

