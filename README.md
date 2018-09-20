# clojure-polyline

Decoding and encoding of the google polyline algorithm as described at
https://developers.google.com/maps/documentation/utilities/polylinealgorithm

# usage

```clojure
[com.michaelgaare/clojure-polyline "0.2.0"]
```

(require com.michaelgaare.clojure-polyline :as polyline)

Decode a polyline string:

(polyline/decode polystring)

Returns a vector of maps: [{:latitude long :longitude long} ...]

Encode a vector of coordinates:

(polyline/encode coords)

returns a polyline-encoded string

-- encode expects a sequence of vectors of the format [latitude
longitude] - if you have this as a sequence of maps of the format
{:longitude longitude :latitude latitude} you can use (coords->vec) to
turn it into the right format.

- you can use to-coords to turn a
vector of [latitude longitude] vectors into this format)

## License

Copyright © 2018 Michael Gaare

Distributed under the Eclipse Public License, the same as Clojure.
