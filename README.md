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

(encode expects a vector of maps - you can use to-coords to turn a
vector of [latitude longitude] vectors into this format)

## License

Copyright © 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
