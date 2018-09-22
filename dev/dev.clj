(ns dev
  (:require [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]

            [criterium.core :as criterium :refer [quick-bench]]
            [com.michaelgaare.clojure-polyline :as cp]))



;;;;; Dev notes

;;;; performance timings:

(def coords [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
(def encoded "_p~iF~ps|U_ulLnnqC_mqNvxq`@")

;;; improvement candidates

(defn partition-bits
  "Takes an integer and the number of bits per segment, returns a vector
   of integers that correspond to the integer broken down into
   segments of the given bitsize."
  [x bits]
  (let [mask (int (dec (reduce * (repeat bits 2))))
        bits (int bits)]
    (loop [rem (int x)
           components []]
      (if (> rem 0)
        (recur (bit-shift-right rem bits)
               (conj components (bit-and rem mask)))
        components))))

(defn coord->integer
  "Returns the coord (a double) multiplied by 100,000 and rounded to
   nearest integer."
  ^long [coord]
  (-> coord
      (* 1e5)
      Math/round))

(defn- invert-negative
  [int]
  (if (neg? int)
    (bit-not int)
    int))

(defn- ascii-char
  [i]
  (char (+ 63 i)))

(defn bit-segments
  "Returns a list of the 5-bit components of integer."
  [x]
  (when (> x 0)
    (cons (bit-and x 31) (bit-segments (bit-shift-right x 5)))))

(defn encode-coord
  "Returns the polyline encoded string of given coordinate (a double
   between -180 and 180)."
  [x]
  (let [coord-int (-> x
                      coord->integer
                      (bit-shift-left 1)
                      invert-negative
                      int)]
    (loop [rem coord-int
           sb (StringBuilder.)]
      (let [nxt-rem (bit-shift-right rem 5)
            chunk (cond-> (bit-and rem 31)
                    (pos? nxt-rem) (bit-or 32))
            sb (.append sb (char (+ chunk 63)))]
        (if (pos? nxt-rem)
          (recur nxt-rem sb)
          (.toString sb))))))

;; (defn compact-coords
;;   "Takes a collection of coord vectors, and returns a collection of vectors of
;;    the difference from the previous coord. The format that polyline wants"
;;   [coords]
;;   (loop [coords coords
;;          compacted (transient [(first coords)])]
;;     (let [[x y] coords]
;;       (if (and x y)
;;         (recur (rest coords) (conj! compacted (mapv - y x)))
;;         (persistent! compacted)))))

(defn compact-coords
  "Takes a vector of coord vectors, and returns a vector of vectors of
   the difference from the previous coord. The format that polyline wants"
    ([coords]
     (when-let [x (first coords)]
       (cons x (compact-coords x (rest coords)))))
    ([x coords]
     (when-let [y (first coords)]
       (let [[xlat xlong] x
             [ylat ylong] y]
         (cons [(- ylat xlat) (- ylong xlong)] (compact-coords y (rest coords)))))))

(defn encode
  "Main polyline encoding function. Takes a collection of [lat long]
   tuples and returns the polyline encoded string representation."
  [coords]
  (transduce (comp cat (map cp/encode-coord))
             str
             (cp/compact-coords coords)))

  ;; (->> coords
  ;;      (partition 2 1)
  ;;      (map (fn [[[x-lat x-long] [y-lat y-long]]]
  ;;             [(- y-lat x-lat) (- y-long x-long)]))
  ;;      (cons (first coords)))


;; (quick-bench (cp/encode coords))

;; Evaluation count : 12672 in 6 samples of 2112 calls.
;;              Execution time mean : 48.192127 µs
;;     Execution time std-deviation : 460.468431 ns
;;    Execution time lower quantile : 47.653945 µs ( 2.5%)
;;    Execution time upper quantile : 48.795429 µs (97.5%)
;;                    Overhead used : 8.607180 ns

;; (quick-bench (cp/decode encoded))

;; Evaluation count : 5772582 in 6 samples of 962097 calls.
;;              Execution time mean : 96.775770 ns
;;     Execution time std-deviation : 1.027468 ns
;;    Execution time lower quantile : 95.587440 ns ( 2.5%)
;;    Execution time upper quantile : 97.803029 ns (97.5%)
;;                    Overhead used : 8.607180 ns

;;; decoding speed is ok, this encoding speed is dreadful

;;; compact-coords looks suspicious

;; (quick-bench (cp/compact-coords coords))

;; Evaluation count : 342288 in 6 samples of 57048 calls.
;;              Execution time mean : 1.773524 µs
;;     Execution time std-deviation : 18.636565 ns
;;    Execution time lower quantile : 1.757375 µs ( 2.5%)
;;    Execution time upper quantile : 1.801366 µs (97.5%)
;;                    Overhead used : 8.607180 ns

;; so this is way too much but not the main offender

;;; encode-coord also looks suspicious
;; it's got a bunch of sequence stuff, maps, reverses, juggling

;; (quick-bench (cp/encode-coord 38.5))

;; Evaluation count : 90486 in 6 samples of 15081 calls.
;;              Execution time mean : 6.642357 µs
;;     Execution time std-deviation : 54.278295 ns
;;    Execution time lower quantile : 6.580419 µs ( 2.5%)
;;    Execution time upper quantile : 6.707147 µs (97.5%)
;;                    Overhead used : 8.607180 ns

;; Horrendous! Especially since this is called for every single lat
;; and long. We found the offender

;;; replacement encode-coord
;; the new one uses a loop and a stringbuilder, no sequence stuff

;; (quick-bench (encode-coord 38.5))

;; Evaluation count : 6016176 in 6 samples of 1002696 calls.
;;              Execution time mean : 91.480910 ns
;;     Execution time std-deviation : 0.494137 ns
;;    Execution time lower quantile : 90.937540 ns ( 2.5%)
;;    Execution time upper quantile : 92.104278 ns (97.5%)
;;                    Overhead used : 8.607180 ns

;;; After dropping in the new encode-coord, here's the timing now
;; reminder that this previously took 48 microseconds

;; (quick-bench (cp/encode coords))

;; Evaluation count : 55026 in 6 samples of 9171 calls.
;;              Execution time mean : 10.756334 µs
;;     Execution time std-deviation : 144.329081 ns
;;    Execution time lower quantile : 10.616531 µs ( 2.5%)
;;    Execution time upper quantile : 10.939354 µs (97.5%)
;;                    Overhead used : 8.607180 ns

;;; But we can do better. Let's look at compact-coords next
;; Tried doing a transducer first but that was dreadful for some
;; reason, twice as slow. Restructured the same code as threaded seq
;; ops. Got deceived by laziness. Actually every time I tried to write
;; nice code it was slow. Somehow that completely insane thing I did 6
;; years ago is faster than every sensible approach I can think of
;; now. You can see my failed ideas littered around the new function,
;; commented out. They all pushed into 3 microseconds.

;; So I just cleaned up the craziness of the original function a bit,
;; got rid of the reverse and the backwards way of building it. Here's
;; the new timing.

;; (quick-bench (compact-coords coords))

;; Evaluation count : 1069980 in 6 samples of 178330 calls.
;;              Execution time mean : 549.331702 ns
;;     Execution time std-deviation : 8.909366 ns
;;    Execution time lower quantile : 540.200353 ns ( 2.5%)
;;    Execution time upper quantile : 560.962365 ns (97.5%)
;;                    Overhead used : 8.952324 ns

;;; Updated encode timing with new compact-coords

;; (quick-bench (cp/encode coords))

;; Evaluation count : 63318 in 6 samples of 10553 calls.
;;              Execution time mean : 9.566284 µs
;;     Execution time std-deviation : 59.373317 ns
;;    Execution time lower quantile : 9.489063 µs ( 2.5%)
;;    Execution time upper quantile : 9.628368 µs (97.5%)
;;                    Overhead used : 8.952324 ns

;;; Finally, try rewriting encode. At mininum, the former use of flatten is maddening
;; tried it as a transducer and we got:

;; (quick-bench (encode coords))

;; Evaluation count : 236424 in 6 samples of 39404 calls.
;;              Execution time mean : 2.538156 µs
;;     Execution time std-deviation : 13.987141 ns
;;    Execution time lower quantile : 2.520797 µs ( 2.5%)
;;    Execution time upper quantile : 2.550994 µs (97.5%)
;;                    Overhead used : 8.952324 ns

;; holy crap that's way better. we have a winner


;;;; Some other stuff I tried


;;; Can we improve on partition-bits?

;; (quick-bench (#'cp/partition-bits 12505 5))

;; Evaluation count : 519756 in 6 samples of 86626 calls.
;;              Execution time mean : 1.150078 µs
;;     Execution time std-deviation : 5.204842 ns
;;    Execution time lower quantile : 1.145342 µs ( 2.5%)
;;    Execution time upper quantile : 1.157427 µs (97.5%)
;;                    Overhead used : 8.607180 ns

;; new partition-bits

;; (quick-bench (partition-bits 12505 5))

;; Evaluation count : 1753770 in 6 samples of 292295 calls.
;;              Execution time mean : 334.419228 ns
;;     Execution time std-deviation : 5.455525 ns
;;    Execution time lower quantile : 330.871941 ns ( 2.5%)
;;    Execution time upper quantile : 343.860254 ns (97.5%)
;;                    Overhead used : 8.607180 ns

;; Found 1 outliers in 6 samples (16.6667 %)
;;  low-severe   1 (16.6667 %)
;;  Variance from outliers : 13.8889 % Variance is moderately inflated by outliers

;;; But maybe actually it would make more sense to partition PLUS pad
;;; at the same time. Actually, let's go after the whole encode-coord
;;; at once
