(ns com.michaelgaare.clojure-polyline-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [com.michaelgaare.clojure-polyline :as polyline]))

(defn- round-to [d]
  (/ (polyline/round (* d polyline/precision)) polyline/precision))

(defn- rand-lat-lon
  "Returns a random coordinate [latitude longitude] with 5 decimal place precision"
  []
  (mapv round-to [(- (rand 180) 90) (- (rand 360) 180)]))

;; test case from google
(def coords [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
(def encoded "_p~iF~ps|U_ulLnnqC_mqNvxq`@")

(deftest test-encoding
  (is (= encoded (polyline/encode coords))))

(deftest test-decoding
  (is (= coords (polyline/decode encoded))))

(deftest test-round-trip
  (let [round (polyline/decode (polyline/encode coords))]
    (is (= coords round))))

(deftest generative-round-trip
  (let [path (repeatedly 2000 rand-lat-lon)
        round (-> path polyline/encode polyline/decode)]
    (is (= path round))))
