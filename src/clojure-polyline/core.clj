(ns clojure-polyline.core)

;; -------------------------------------------------------
;; Utility functions
;; -------------------------------------------------------

(defn partition-by-inclusive
  "like partition-by, but also puts the first non-matching element
  in the split, and only groups results that return true in the pred f"
  [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [run (take-while #(f %) s)
           rem (seq (drop (count run) s))
           included (first rem)
           run-inc (concat run (vector included))]
       (cons run-inc (partition-by-inclusive f (rest rem)))))))

(defn combiner
  "takes a function and a sequence, and then returns a sequence of applying the
  function to the first element in the sequence, and then the result of that and
  the second element, and so on"
  ([f xs] (lazy-seq
           (when-let [s (seq xs)]
             (let [run (map f (first s))]
               (cons run (combiner f run (rest s)))))))
  ([f x y] (lazy-seq
            (when-let [s ( seq y)]
              (let [run (map f x (first s))]
                (cons run (combiner f run (rest s))))))))

(defn split [ints]
  (partition-by-inclusive #(> % 31) ints))

;; -------------------------------------------------------
;; Decode functions
;; -------------------------------------------------------

(defn decode-chunk [c]
  (let [pc (reduce #(+ (bit-shift-left %1 5) %2) (reverse (map #(bit-and-not % 32) c)))
        neg (= 1 (mod pc 2))]
    (/ (bit-shift-right (if neg (bit-not pc) pc) 1) 100000)))

(defn decode [polystring]
  (let [poly-ints (map #(- % 63) (map int polystring))
        poly-chunks (split poly-ints)
        decoded-chunks (map decode-chunk poly-chunks)]
    (->> decoded-chunks (map double) (partition 2) (combiner +)
         (map #(hash-map :latitude (first %) :longitude (first (rest %)))))))

;; -------------------------------------------------------
;; Encode functions
;; -------------------------------------------------------

