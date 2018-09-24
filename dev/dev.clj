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

(def coords [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
(def encoded "_p~iF~ps|U_ulLnnqC_mqNvxq`@")

;;;;; Dev notes

;;;; Let's try rewriting `decode` as a transducer

(defn partition-at
  "Somewhat similar to partition-by. Takes a predicate, and splits after
   every time predicate returns a truty value.

   Only supports transducer arity because that's all we use here."
  [pred]
  (fn [rf]
    (let [a (java.util.ArrayList.)]
      (fn
        ([] (rf))
        ([result]
         (let [result (if (.isEmpty a)
                        result
                        (let [v (vec (.toArray a))]
                          (.clear a)
                          (unreduced (rf result v))))]
           (rf result)))
        ([result input]
         (.add a input)
         (if (pred input)
           (let [v (vec (.toArray a))]
             (.clear a)
             (rf result v))
           result))))))

(defn decode-to-split
  [polystring]
  (into []
        (comp (map int)
              (map #(- % 63))
              (partition-at #(< % 32))

              )
        polystring))

(defn poly-number
  "Transducer that transforms characters from a polyline string into the
   raw decoded numbers, without double or negative conversion."
  [rf]
  (let [acc (volatile! 0)
        seen (volatile! 0)]
    (fn
      ([] (rf))
      ;; if we get stopped with leftover accumulator, nothing to be
      ;; done about it
      ([result] (rf result))
      ([result c]
       (let [c-int (-> c int (- 63))
             complete? (< c-int 32)
             cur (vswap! acc +
                         (-> c-int
                             (bit-and-not 32)
                             (bit-shift-left (* @seen 5))))]
         (if complete?
           (do
             (vreset! acc 0)
             (vreset! seen 0)
             (rf result cur))
           (do
             (vswap! seen inc)
             result)))))))

(defn decode-to-int
  [polystring]
  (into []
        poly-number
        polystring
        )
  )

(defn restore-negative
  "Restores x to negative, if x is polyline encoded as negative."
  [x]
  (cond-> x
    (bit-test x 0) bit-not))

(defn decode-to-double
  [polystring]
  (into []
        (comp poly-number
              (map restore-negative)
              (map #(bit-shift-right % 1))
              (map #(/ % 100000))
              (map double))
        polystring
        )
  )


(defn decode
  [polystring]
  (->> polystring
       (sequence (comp poly-number
                       (map restore-negative)
                       (map #(bit-shift-right % 1))
                       (map #(/ % 100000))
                       (map double)
                       (partition-all 2)))
       (reductions (fn [[xlat xlong] [ylat ylong]]
                     [(+ xlat ylat) (+ xlong ylong)]))))

(->> [32 49 63 42 7]
     (map #(bit-and-not % 32))
     (reduce #(+ (bit-shift-left %1 5) %2)))


;; expected result:
(->> encoded
     (map int)
     (map #(- % 63))
     cp/split)

;; ((32 49 63 42 7) (63 49 52 61 22) (32 54 45 13) (47 47 50 4) (32 46 50 15) (55 57 50 33 1))

;;; quick reminder of current performance
;; almost got fooled by lazy seq!
;; (quick-bench (cp/decode encoded))
;; was just 98 ns. But with doall...

;; (quick-bench (doall (cp/decode encoded)))

;; Evaluation count : 14598 in 6 samples of 2433 calls.
;;              Execution time mean : 42.386430 µs
;;     Execution time std-deviation : 2.424148 µs
;;    Execution time lower quantile : 41.163051 µs ( 2.5%)
;;    Execution time upper quantile : 46.593247 µs (97.5%)
;;                    Overhead used : 8.952324 ns

;; Ah hah! Almost as bad as encode was

;;;; Just getting through split in the current code takes 14.5 microseconds
;;;; transducer version
;;; (quick-bench (decode-to-split encoded)) ;; => 4.6 microseconds. 10 less!

;;;; Now performance of the whole shebang:

;; (quick-bench (doall (decode encoded)))

;; Evaluation count : 59592 in 6 samples of 9932 calls.
;;              Execution time mean : 10.086442 µs
;;     Execution time std-deviation : 99.443224 ns
;;    Execution time lower quantile : 9.981137 µs ( 2.5%)
;;    Execution time upper quantile : 10.239410 µs (97.5%)
;;                    Overhead used : 8.952324 ns

;; only a 4x increase, ohhh well
