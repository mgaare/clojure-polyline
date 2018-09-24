# clojure-polyline

Decoding and encoding of the google polyline algorithm as described at
https://developers.google.com/maps/documentation/utilities/polylinealgorithm

# Releases

Current stable release is 0.3.0, which requires Clojure 1.7 or later.

[Leiningen](https://github.com/technomancy/leiningen) dependency information:
```clojure
[com.michaelgaare/clojure-polyline "0.3.0"]
```

# Usage

```clojure
(require '[com.michaelgaare.clojure-polyline :as polyline])

;; Decode a polyline string:
(polyline/decode "_p~iF~ps|U_ulLnnqC_mqNvxq`@")
;; => ({:longitude -120.2, :latitude 38.5} {:longitude -120.95, :latitude 40.7} {:longitude -126.453, :latitude 43.252})

;; Encode a collection of coordinates:
(polyline/encode [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
;; => "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
```

`encode` expects a sequence of vectors of the format `[latitude
longitude]` - if you have this as a sequence of maps of the format
`{:longitude longitude :latitude latitude}` you can use `coords->vec` to
convert to a collectoin of coordinate vectors.

You can use also use `to-coords` to turn a collection of `[latitude
longitude]` vectors into the map format.

## Changelog

* Release 0.3.0 on 2018-09-24
  * Minimum Clojure version is now 1.7 (because transducers)
  * The `encode` codepath completely rewritten to scrub out some of
    the wacky stuff I did 6 years ago. As a result it's 19x faster.

## License

Copyright © 2018 Michael Gaare

Distributed under the Eclipse Public License, the same as Clojure.
