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

(deftest test-round-trip
  (let [round (coords->vec (decode (encode coords)))]
    (is (= (count round) (count coords)))
    (is (every? (fn [[x y]] (close-enough? x y))
                (map vector (flatten round) (flatten coords))))))


(def test-coords [[61.55277999999998 23.778589999999994] [ 61.55432999999998 23.777489999999993] [61.555079999999975 23.777199999999993] [61.55683999999997 23.775149999999993]])
(def test-polyline "ielvJehqoCdc@llAdVvgBpIbbBj[fqFzWxnB")


(deftest decode-returns-correct-amount-of-coords
  (is (= 6 (count (decode test-polyline)))))

(deftest encode-and-decode-returns-correct-amount-of-coords
  (is (= 4 (count (decode (encode test-coords))))))
