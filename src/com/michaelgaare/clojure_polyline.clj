(ns com.michaelgaare.clojure-polyline
  "Functions to encode and decode Google polyline algorithm.")

;; -------------------------------------------------------
;; Utility functions
;; -------------------------------------------------------

(defn vec->coords [coord-vec]
  (map (fn [[lat long]] (hash-map :latitude lat :longitude long))
       coord-vec))

(defn coords->vec [coords]
  (map (fn [{:keys [latitude longitude]}] [latitude longitude]) coords))

(defn latlon
  "Converts a coordinate in [lon lat] format to [lat lon]."
  [[lon lat :as coord]]
  [lat lon])

(defn lonlat
  "Converts a coordinate in [lat lon] format to [lon lat]."
  [[lat lon :as coord]]
  [lon lat])

;; -------------------------------------------------------
;; Decode functions
;; -------------------------------------------------------

(defn- restore-negative
  "Restores x to negative, if x is polyline encoded as negative."
  [x]
  (cond-> x
    (bit-test x 0) bit-not))

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

(defn decompress
  "Transducer that decompresses polyline coordinates that have been
   compressed by turning them into deltas from the previous coord."
  [rf]
  (let [prev (volatile! nil)]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result [lat lon]]
       (let [decompressed (if-let [[prev-lat prev-lon] @prev]
                            [(+ lat prev-lat) (+ lon prev-lon)]
                            [lat lon])]
         (vreset! prev decompressed)
         (rf result decompressed))))))

(def decoder
  "Transducer stack for decoding."
  (comp poly-number
        (map restore-negative)
        (map #(bit-shift-right % 1))
        (map #(/ % 100000))
        (map double)
        (partition-all 2)
        decompress))

(defn decode
  "Takes a polyline-encoded string and returns a collection of
   decoded [lat long] coordintes."
  [polystring]
  (into [] decoder polystring))

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

(defn compress
  "Transducer that compresses polyline coordinates by turning them into
   deltas from the previous coord."
  [rf]
  (let [prev (volatile! nil)]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result [lat lon]]
       (let [compressed (if-let [[prev-lat prev-lon] @prev]
                          [(- lat prev-lat) (- lon prev-lon)]
                          [lat lon])]
         (vreset! prev [lat lon])
         (rf result compressed))))))

(def encoder
  "Transducer stack for encoding."
  (comp compress
        cat
        (map encode-coord)))

(defn encode
  "Main polyline encoding function. Takes a collection of [lat long]
   tuples and returns the polyline encoded string representation."
  [coords]
  (transduce encoder str coords))
