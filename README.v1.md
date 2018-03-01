# railway-oriented-clj

Or how to stop throwing exceptions, throw away nil and explicitly deal with your failures.

[![Build Status](https://travis-ci.org/HughPowell/railway-oriented-clj.svg?branch=master)](https://travis-ci.org/HughPowell/railway-oriented-clj)
[![Clojars Project](https://img.shields.io/clojars/v/uk.co.hughpowell/railway-oriented-clj.svg)](https://clojars.org/uk.co.hughpowell/railway-oriented-clj)

A library for explicit error handling based on [Railway Oriented Programming](https://fsharpforfunandprofit.com/posts/recipe-part2/).  I strongly suggest reading and understanding the article before diving in with this library.

While the article does a good job of introducing the concepts and provides, what I assume is, an idiomatic F# implementation, I'm hoping we can build something more recognisable to the average Clojurian.  With a bit of luck we'll manage to do away with the throwing of Exceptions and nil values simultaneously.

## What's all this about then?

The aim of this library is to provide alternative implementations to the clojure.core functions and macros that are responsible for composing other functions.  These new functions and macros short circuit when an error or nil result is encountered.

### Some Definitions

**Result object** result of a function, indicating the state of the result and containing either a value or an error.  
**Regular function** a function that takes one or more parameters and returns a value (possibly including nil).  
**Switch function** a function that takes one or more parameters and returns a result object.  
**Bound function** a function that takes a result object as a parameter and returns a result object.  If the parameter result object is a failure then this is returned immediately.  This is synonymous with the two track input functions described in the article.  Can be composed using regular composition functions and macros.

### Basic usage

For Leiningen, add the following to the dependencies key in project.clj

    :dependencies
        [...
         [uk.co.hughpowell/railway-oriented-clj "0.2.5"]
         ...]

Then just require the public namespaces

    (ns ... 
      (:require ... 
        [uk.co.hughpowell.railway-oriented-clj.v2.public.... :as ...]
        ...))

as per usual.

There are 4 namespaces that we should use:
 * `result` - constructors and handlers for result objects
 * `adapters` - functions for adapting already existing functions to be used with railway oriented programming
 * `bind` - reimplementations of the clojure.core functions and macros that take and short circuit switch functions
 * `lift` - reimplementations of the clojure.core functions and macros that take and short circuit bound functions

See the `examples.clj` namespace in the `test` package for translations of the examples given in the article.

## Other Projects

There's a couple of other projects with a focus on more closely translating the functionality in the article. [One](https://gist.github.com/ah45/7518292c620679c460557a7038751d6d) using the Clojure cats library and [another](https://github.com/jwillem/rop-clojure) that's under active development.

## Hacking on this Library

To download and install the library locally

    git clone git@github.com:HughPowell/railway-oriented-clj.git
    cd railway-oriented-clj
    lein install
    cd ..

To then use it in your project add it to the projects dependencies in project.clj

    :dependencies
        [...
         [uk.co.hughpowell/railway-oriented-clj "0.2.6-SNAPSHOT"]
         ...]

## Ownership and License

Copyright © 2017 Hugh Powell

The contributors are listed in AUTHORS. This project uses the [MPL v2 license](https://www.mozilla.org/en-US/MPL/2.0/), see LICENSE.

railway-oriented-clj uses the [C4 (Collective Code Construction Contract)](https://rfc.zeromq.org/spec:42/C4) process for contributions.

railway-oriented-clj uses the [clojure-style-guide](https://github.com/bbatsov/clojure-style-guide) for code style.

To report an issue, use the railway-oriented-clj issue tracker at [github.com](https://github.com/HughPowell/railway-oriented-clj/issues).