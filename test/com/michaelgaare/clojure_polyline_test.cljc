(ns com.michaelgaare.clojure-polyline-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [com.michaelgaare.clojure-polyline :as polyline]))

(defn- round [d]
  (/ (polyline/round (* d polyline/precision)) polyline/precision))

(defn- rounded [coords]
  (map round coords))

(defn- rand-lat-lon []
  [(- (rand 180) 90) (- (rand 360) 180)])

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
  (let [coords (repeatedly 2000 rand-lat-lon)
        rounded (map rounded coords)]

    (testing "Coordinates are round to 5 dp during encoding"
      (let [encoded-coords (polyline/encode coords)
            encoded-rounded (polyline/encode rounded)]
        (is (= encoded-coords encoded-rounded))))

    (testing "the decoded results are to 5dp"
      (let [actual (-> coords polyline/encode polyline/decode)]
        (is (not= coords actual))
        (is (= rounded actual))))))
