(ns clojure-polyline.core-test
  (:use clojure-polyline.core)
  (:use clojure.test))

(def test-coords [[61.55277999999998 23.778589999999994] [ 61.55432999999998 23.777489999999993] [61.555079999999975 23.777199999999993] [61.55683999999997 23.775149999999993]])
(def test-polyline "ielvJehqoCdc@llAdVvgBpIbbBj[fqFzWxnB")


(deftest decode-returns-correct-amount-of-coords
  (is (= 6 (count (decode test-polyline)))))

(deftest encode-and-decode-returns-correct-amount-of-coords
  (is (= 4 (count (decode (encode test-coords))))))

