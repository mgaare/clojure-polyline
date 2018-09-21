(ns user
  (:require [clojure.tools.namespace.repl :refer :all]))

(defn dev
  []
  (require 'dev)
  (in-ns 'dev))
