(defproject com.michaelgaare/clojure-polyline "0.3.0-SNAPSHOT"
  :description "library to encode and decode google polyline algorithm"
  :url "http://github.com/mgaare/clojure-polyline"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [criterium "0.4.4"]]
                   :source-paths ["dev"]}})
