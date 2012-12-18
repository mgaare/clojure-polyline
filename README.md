# clojure-polyline

Decoding and encoding of the google polyline algorithm

# usage

```clojure
[clojure-polyline "0.1"]
```

(require clojure-polyline.core :as polyline)

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

Copyright © 2012 Michael Gaare

Distributed under the Eclipse Public License, the same as Clojure.
