# partial-fn

Wikipedia defines "partial function" as:

> In mathematics, a partial function from X to Y (written as f: X ↛ Y) is a function f: X' → Y, where X' is a subset of X. It generalizes the concept of a function f: X → Y by not forcing f to map every element of X to an element of Y (only some subset X' of X). If X' = X, then f is called a total function and is equivalent to a function. Partial functions are often used when the exact domain, X' , is not known (e.g. many functions in computability theory).

## Partial functions in Scala

Scala makes the concept of partial functions first-class by providing a dedicated type and syntactic support for the same. Let's see why this is useful.

Consider the following piece of code (pasted directly from a REPL session):

```scala
scala> def foo(a: String, b: String) = try {
     |   a.toInt / b.toInt
     | } catch {
     |   case ex: NumberFormatException => 'nfe
     |   case ex: ArithmeticException => 'ae
     | }
foo: (a: String, b: String)Any

scala> foo("2", "1")
res6: Any = 2

scala> foo("kl", "1")
res7: Any = 'nfe

scala> foo("9", "0")
res8: Any = 'ae
```

Syntactically, the `try`-`catch` here looks fairly similar to its Clojure counterpart. However there is one big difference. The bit that's passed to `catch` as argument is a first-class **value**! This lets us do things like:

```scala
scala> def foo(a: String, b: String, handler: PartialFunction[Throwable, Any]) = try {
     |   a.toInt / b.toInt
     | } catch handler
foo: (a: String, b: String, handler: PartialFunction[Throwable,Any])Any

scala> val h: PartialFunction[Throwable, Any] = {
     |   case ex: NumberFormatException => 'nfe
     | }
h: PartialFunction[Throwable,Any] = <function1>

scala> val i: PartialFunction[Throwable, Any] = {
     |   case ex: ArithmeticException => 'ae
     | }
i: PartialFunction[Throwable,Any] = <function1>

scala> foo("4", "0", h.orElse(i))
res9: Any = 'ae
```

And even:

```scala
scala> attempt {
     |   val s = Console.readLine
     |   s.toInt
     | } fallback {
     |   case ex: NumberFormatException => println("Invalid string. Try again."); restart
     | }

// "hobo"
Invalid string. Try again.

// "kucuk"
Invalid string. Try again.

// "9"
res12: Int = 9

scala>
```

*(You can find the implementation for the `attempt`-`fallback` utility [here](http://blog.engineering.vayana.in/).)*

From these examples, we can deduce two major advantages of first-class partial functions:

- One can compose partial functions, using combinators such as `orElse`.
- It's easy to create new constructs requiring case-based handling, without having to resort to ad hoc syntactic transformations (macros).

There are numerous examples in the Scala world where this has been put to a good use. Some of which are as follows:

- [`scala.util.control.Exception`](http://www.scala-lang.org/api/current/index.html#scala.util.control.Exception$) - Compositional and functional goodness, atop traditional exception handling constructs.
- Standard collection operations like `collect`.
- Methods such as [`onSuccess`](http://docs.scala-lang.org/overviews/core/futures.html#callbacks) for registering callbacks in futures library.
- Error recovery combinators, such as [`recover`](http://docs.scala-lang.org/overviews/core/futures.html#functional_composition_and_forcomprehensions) in futures library.
- [`react`](http://docs.scala-lang.org/overviews/core/actors.html) block in actors.
- [Request matchers](http://simply.liftweb.net/index-Chapter-11.html) in Lift.

## How things look on the Clojure side

Clojure currently does not have a generic partial function construct. Clojure's `try`-`catch` for example, is an ad hoc syntax, which maps almost directly to its Java counterpart.

As it happens, Clojure has everything you may need to implement this idea on your own:

- First-class functions.
- [`core.match`](https://github.com/clojure/core.match) - a great pattern matching library to piggyback on.
- Support for syntactic extensions (by virtue of being a Lisp).

`partial-fn` project uses the above to provide a simple partial function implementation for Clojure.

## What the project currently does

The REPL session below should demystify the crux of the library:

```clojure
user=> (use '[clojure.core.match :only (match)]) (use 'partial-fn.core)
nil
nil
user=> (use '[clojure.pprint :only (pprint)])
nil
user=> (macroexpand-1 '(partial-fn [a b] [2 :two] :qux [3 :three] :guz))
(partial-fn.core/map->PartialFunction {:fun        (clojure.core/fn [a b]
                                                     (clojure.core.match/match [a b]
                                                                               [2 :two] :qux
                                                                               [3 :three] :guz))
                                       :in-domain? (clojure.core/fn [a b]
                                                     (clojure.core.match/match [a b]
                                                                               [2 :two] true
                                                                               [3 :three] true
                                                                               :else false))})
```


## Future directions for the library

- Combinators to compose, transform partial functions. Examples: `or-else`, `comp`, `apply-or-else`, `lift`, `unlift`, `cond` etc.
- Scala's `PartialFunction`s have more special treatment in compiler, making the above-mentioned combinators very efficient. We could borrow some of those ideas in this port.
- A variant of `try`-`catch` that accepts its handler as a partial function value.
- The [`slingshot`](https://github.com/scgilardi/slingshot) library has a concept of "selectors". I think "selectors" are simply a special case of "matching", and matching should belong in `core.match`. The selectors could likely be reimplemented with a bunch of custom `core.match` patterns, plus `partial-fn`.
- The partial function implementation could potentially make use of knowledge of `core.match` innards to provide faster implementations of `:fun` and `:in-domain?`.


## Usage

(REPL session again.)

```clojure
user=> (use '[clojure.core.match :only (match)]) (use 'partial-fn.core)
nil
nil
user=> (define-partial-fn foo [a b]
  #_=>   [3 1] :nice
  #_=>   :else :aww-shucks)
#'user/foo
user=> (foo 3 1)
:nice
user=> (foo 3 2)
:aww-shucks
user=> (in-domain? foo 3 1)
true
user=> (in-domain? foo 3 3)
true
user=> (define-partial-fn foo [a b]
  #_=>   [3 1] :nice)
#'user/foo
user=> (foo 3 2)

IllegalArgumentException No matching clause: 3 2  user/fn--2398 (NO_SOURCE_FILE:1)
user=> (in-domain? foo 3 2)
false
user=> Bye for now!%
```

## Inputs welcome!

Any sort of feedback, code review, pull requests are most welcome!

## License

Copyright © 2014 Rahul Goma Phulore.

Distributed under the Eclipse Public License, the same as Clojure.
