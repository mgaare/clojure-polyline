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

(defn to-coords [coord-vec]
  (map (fn [[lat long]] (hash-map :latitude lat :longitude long))
       coord-vec))

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
         (to-coords))))

;; -------------------------------------------------------
;; Encode functions
;; -------------------------------------------------------

(defn coord->int [coord]
  (-> coord
      (* 1e5)
      Math/rint
      int))

(defn invert-negative [int]
  (if (neg? int)
    (bit-not int)
    int))

(defn chunk-bits [int]
  (let [bitstring (Integer/toString int 2)]
    (->> bitstring
         reverse
         (partition 5 5 nil)
         (map reverse)
         (map #(apply str %)))))

(defn bin->int [bin]
  (Integer/parseInt bin 2))

(defn bins->padded-ints [bins]
  (let [decimals (map bin->int bins)
        end (last decimals)]
    (->> decimals
         drop-last
         (map (+ 32))
         reverse
         (cons end)
         (map (+ 63))
         reverse)))

(defn ints->str [ints]
  (->> ints
       (map char)
       (apply str)))

(defn encode-coord [coord]
  (-> coord
      coord->int
      (bit-shift-left 1)
      invert-negative
      chunk-bits
      bins->padded-ints
      ints->str))