# clojure-polyline

Decoding and encoding of the google polyline algorithm as described at
https://developers.google.com/maps/documentation/utilities/polylinealgorithm

[![CircleCI](https://circleci.com/gh/jesims/clojure-polyline.svg?style=svg)](https://circleci.com/gh/jesims/clojure-polyline)

[![Clojars Project](https://img.shields.io/clojars/v/io.jesi/clojure-polyline.svg)](https://clojars.org/io.jesi/clojure-polyline)

# Usage

```clojure
(require '[com.michaelgaare.clojure-polyline :as polyline])

;; Decode a polyline string:
(polyline/decode "_p~iF~ps|U_ulLnnqC_mqNvxq`@")
;; => [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]]

;; Encode a collection of coordinates:
(polyline/encode [[38.5 -120.2] [40.7 -120.95] [43.252 -126.453]])
;; => "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
```

`encode` expects a collection of vectors of the format `[latitude
longitude]`, and returns the polyline encoded string.

`decode` returns a vector of `[latitude longitude]` vectors for a
given polyline encoded string.

You can add additional transforms to the encoder and decoder
transducer stacks, which are bound to `encoder` and `decoder` vars
respectively. For instance, to decode a polyline string as `[longitude
latitude]` pairs as some systems expect, you can do:

```clojure
(into [] (comp polyline/decoder (map polyline/lonlat)) your-polystring)`
```

## Changelog

* Release 0.4.1 on 2018-11-07
  * Support for ClojureScript
  * 5 decimal place encode and decode (aligning with the google algorithm)

* Release 0.4.0 on 2018-10-01
  * BREAKING CHANGE: the decode API now matches the encode API:
    encode was always `[[lat lon]] => String`, now decode is `String => [[lat lon]]`.
  * The `decode` codepath was completely rewritten as transducers,
    about 4x performance improvement.
  * Removed the old transform utility functions that worked in terms
    of maps with `:latitude` and `:longitude` keys
  * Exposed access to the encoding and decoding transducer stacks as
    the `encoder` and `decoder` vars respectively
  * Replaced the old `compact-coords` function - my least favorite
    code remaining in the project - with a `compress` transducer

* Release 0.3.0 on 2018-09-24
  * Minimum Clojure version is now 1.7 (because transducers)
  * The `encode` codepath completely rewritten to scrub out some of
    the wacky stuff I did 6 years ago. As a result it's 19x faster.

## Contributors

* [Alex Scott (AXRS)](https://github.com/axrs)

## License

Copyright © 2018 Michael Gaare

Distributed under the Eclipse Public License, the same as Clojure.
