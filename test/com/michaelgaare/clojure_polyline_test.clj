(ns com.michaelgaare.clojure-polyline-test
  (:use com.michaelgaare.clojure-polyline
        clojure.test))

(defn close-enough?
  "Fuzzy comparison for floats. If the difference is less than 1/100,000th we're good!"
  [x y]
  (let [max-diff (Math/abs (/ x 100000))]
    (> max-diff
       (Math/abs (- x y)))))

;; test case from google

(def coords [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
(def encoded "_p~iF~ps|U_ulLnnqC_mqNvxq`@")

(deftest test-encoding
  (is (= encoded (encode coords))))

(deftest test-decoding
  (is (every? (fn [[x y]] (close-enough? x y))
              (map vector (flatten coords) (flatten (decode encoded))))))

(deftest test-round-trip
  (let [round (decode (encode coords))]
    (is (= (count round) (count coords)))
    (is (every? (fn [[x y]] (close-enough? x y))
                (map vector (flatten round) (flatten coords))))))
