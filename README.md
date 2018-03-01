# railway-oriented-clj

Or how to stop throwing exceptions, discard nil and explicitly deal with
your failures.

[![Build Status](https://travis-ci.org/HughPowell/railway-oriented-clj.svg?branch=master)](https://travis-ci.org/HughPowell/railway-oriented-clj)
[![Clojars Project](https://img.shields.io/clojars/v/uk.co.hughpowell/railway-oriented-clj.svg)](https://clojars.org/uk.co.hughpowell/railway-oriented-clj)

A library for explicit error handling in Clojure based on the excellent
blogs ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/)
and  [Railway Oriented Programming](https://fsharpforfunandprofit.com/posts/recipe-part2/).

The "Good Enough" blog introduces the concept of explicit error handling
to Clojure, but the macro introduced sort of works like the ->> macro
from the core library, but not quite.  Railway Oriented Programming
provides a more comprehensive explanation and demonstration of
explicit error handling in function languages. This library, in the best
tradition of Clojure, cherry picks the best from both articles and mixes
in some real world experience to, hopefully, provide idiomatic, easy to 
understand and easy to use explicit error handling in Clojure.

[Version 1 README](./README.v1.md)

## What's all this about then?

Railway Oriented Clj aims to bring explicit error handling to
Clojure in a way that is easy to understand and use.  As far as possible
it uses concepts already familiar to Clojure programmers and provides as
few surprises as possible.

Obligatory Clojure library quote

> “Negative results are just what I want. They’re just as valuable to me
> as positive results. I can never find the thing that does the job best
> until I find the ones that don’t.” ― Thomas A. Edison

## Installation


For Leiningen, add the following to the dependencies key in project.clj

    :dependencies
        [...
         [uk.co.hughpowell/railway-oriented-clj "0.3.0"]
         ...]

Then just require the core namespace

    (ns ... 
      (:require ... 
        [uk.co.hughpowell.railway-oriented-clj.v3.core :as roc]
        ...))

as per usual.

## How is a failure defined?

By default Exceptions and `nil` are treated as failures.  `nil` 
is converted to a NullPointerException at the point at which it is
encountered.

Both parameters and return values of functions are tested as to whether
they are failures or not.

If you want to customise how a failure is defined you can do so using
the `roc/set-...!` functions.  For example, if you want to define
a failure as a map with keys :type and :message that take a keyword and 
string respectively then you could do the following:

```clojure
(ns my-project.core
  (:require [uk.co.hughpowell.railway-oriented-clj.v3.core :as roc]))
  
(roc/set-failure?-fn!
  (fn [value}]
    (and (= (count value) 2))
         (keyword? (:type value))
         (string? (:message value)))))
         
(roc/set-exception-handler?
  (fn [exception]
    {:type    :unexpected-excption
     :message (.getMessage exception)}))

(roc/set-nil-handler?
  (constantly {:type    :nil
               :message "nil value detected"}))
  
```

## Sequential flow control

The bulk of this library consists of macros and functions for explicitly
handling sequential flow control.

The threading macros, `roc/->`, `roc/->>` and `roc/as->`,
act in a very similar way to their core counterparts, with two
exceptions.

1. If a failure is passed to any of the forms or a failure is generated 
when the the failure is evaluated then that failure is returned
immediately and no subsequent forms are evaluated.
2. Only Symbols representing functions and forms that are function calls
may be passed as parameters to these macros. So no macros or Java
interop at the top level.

`roc/when-let` is similar to the core `when-let`, but allows multiple
pairs of bindings (like `let`) and if one of those bindings returns a 
failure evaluation is halted and the failure is returned.  Again, only
forms that are function calls can be used.

`roc/comp` is similar to the core version of `comp` except that it will
short circuit should it encounter a failure.

### Examples

This example assumes we've defined failures as we did in the last
section.

```clojure
(ns my-project.core
  (:require [uk.co.hughpowell.railway-oriented-clj.v3.core :as roc]))
  
(defn read-from-database [connection data-id]
  (try
     ...     
     data-from-database
     (catch Exception e
       {:type :database-read-error
        :message (.getMessage e)})))
       
(defn merge-data [new-data data-from-database]
  ...
  (if-some [merged-data ...]
    merged-data
    {:type :merge-failure
     :message (str "Failed to merge "
                   input-data 
                   " and " 
                   data-from-database)}))
  
(defn save-to-database [connection merged-data]
  (try
    ...
    merged-data
    (catch Exception e
      {:type database-write-error
       :message (.getMessage e)})))

(defn merge-new-data
  [connection data-id new-data]
  (roc/->> (read-from-database connection data-id)
           (merge-data new-data)
           (save-to-database connection)))
```

In this case the `roc/->>` acts a lot like the core `->>`
macro except if any of the forms return a failure no further forms are
evaluated and that failure is returned.  So if an exception is thrown
while `read-from-database` is being evaluated, an error is returned and
no other forms are evaluated.

`roc/->` acts in a similar way and you could re-write `merge-new-date`
with `roc/as->` like so

```clojure
(defn merge-new-data
  [connection data-id new-data]
  (roc/as-> data-id $
            (read-from-database connection $)
            (merge-data new-data $)
            (save-to-database connection$)))
```

What happens when we want to use the result of a function that, might
fail, in multiple subsequent forms.  Normally we'd use `let` (or
`when-let` if we only wanted to bind one result and not do anything if 
that result was `nil`).

```clojure
(let [result (get-result ...)]
  (function-using-result result)
  (other-funciton-using-result result))
```

But what if `get-result` returns a failure.  That's where `roc/when-let`
steps in.  It can take multiple binding forms and returns a failure once
one of those forms returns a failure.

```clojure
(roc/when-let [result-1 (get-result-1 ...)
               result-2 (get-result-2 ...)
               result-3 (get-result-3 result-1 ...)]
  (do-something result-2 result-3))
```

If `get-result-2` returns a failure `get-result-3` and `do-something`
are never evaluated and the whole evaluation returns the failure.

## Parallel flow control

Sometimes we want to evaluate multiple forms that may return a failure
but don't depend on each other.  For this we have `roc/combine`.  This
takes a sequence of results and if they are all successes  applies a
success function.  Otherwise applies a failure function to the failures.

### Example

```clojure
(let [result-1 (get-result-1 ...)
      result-2 (get-result-2 ...)
      result-3 (get-result-3 ...)]
  (roc/combine identity first [result-1 result-2 result3]))
```

Here if all the results are successes then we return a sequence of 3
results. If there are any failures we just return the first one.

## Dealing with failures

Once we actually want to deal with a failure we need `roc/if-let`.
This is just like the core `if-let` except that the else branch is taken
if the binding represents a failure.

### Example

```clojure
(if-let [result (get-result ...)]
  (return-success result)
  (do 
    (log-error result)
    (return-failure result)
```

## Other Projects

There's a couple of other projects with a focus on more closely
translating the functionality in Raylway Oriented Programming.
[One](https://gist.github.com/ah45/7518292c620679c460557a7038751d6d)
using the Clojure cats library and
[another](https://github.com/jwillem/rop-clojure) that's under active
development.

## Hacking on this Library

To download and install the library locally

    git clone git@github.com:HughPowell/railway-oriented-clj.git
    cd railway-oriented-clj
    lein install
    cd ..

To then use it in your project add it to the projects dependencies in project.clj

    :dependencies
        [...
         [uk.co.hughpowell/railway-oriented-clj "0.3.1-SNAPSHOT"]
         ...]

## Ownership and License

Copyright © 2018 Hugh Powell

The contributors are listed in AUTHORS. This project uses the [MPL v2 license](https://www.mozilla.org/en-US/MPL/2.0/), see LICENSE.

railway-oriented-clj uses the [C4 (Collective Code Construction Contract)](https://rfc.zeromq.org/spec:42/C4) process for contributions.

railway-oriented-clj uses the [clojure-style-guide](https://github.com/bbatsov/clojure-style-guide) for code style.

To report an issue, use the railway-oriented-clj issue tracker at [github.com](https://github.com/HughPowell/railway-oriented-clj/issues).