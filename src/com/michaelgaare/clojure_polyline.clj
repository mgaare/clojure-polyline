(ns com.michaelgaare.clojure-polyline)

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

(defn vec->coords [coord-vec]
  (map (fn [[lat long]] (hash-map :latitude lat :longitude long))
       coord-vec))

(defn coords->vec [coords]
  (map (fn [{:keys [latitude longitude]}] [latitude longitude]) coords))

(defn- coord->int
  [coord]
  (-> coord
      (* 1e5)
      Math/round))

(defn ints->str [ints]
  (->> ints
       (map char)
       (apply str)))

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
         (vec->coords))))

;; -------------------------------------------------------
;; Encode functions
;; -------------------------------------------------------

(defn- invert-negative [int]
  (if (neg? int)
    (bit-not int)
    int))

(defn coord->integer
  "Returns the coord (a double) multiplied by 100,000 and rounded to
   nearest integer."
  ^long [coord]
  (-> coord
      (* 1e5)
      Math/round))

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
  (transduce (comp cat (map encode-coord))
             str
             (compact-coords coords)))
