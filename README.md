# Scala JSON AST

[![Join the chat at https://gitter.im/mdedetrich/scala-json-ast](https://badges.gitter.im/mdedetrich/scala-json-ast.svg)](https://gitter.im/mdedetrich/scala-json-ast?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Two minimal implementations of a [JSON](https://en.wikipedia.org/wiki/JSON) `AST`, one that is designed for
typical use and another that is designed for performance.

## Standard AST
Implementation is in `scala.json.ast.JValue`

### Goals
- Fully immutable (all underlying collections/types used are immutable)
- `constant`/`effective constant` lookup time for `JArray`/`JObject`
- Adherence to the typical use for the [JSON](https://en.wikipedia.org/wiki/JSON) standard.
    - Number representation for `JNumber` is a `BigDecimal` (http://stackoverflow.com/a/13502497/1519631)
    - `JObject` is an actual `Map[String,JValue]`. This means that it doesn't handle duplicate keys for a `JObject`,
    nor does it handle key ordering.
    - `JArray` is an `Vector`
- Library does not allow invalid JSON in the representation and hence we can guarantee that a `JValue` will 
always contain a valid structure that can be serialized/rendered into [JSON](https://en.wikipedia.org/wiki/JSON). 
There is one exception, and that is for `scala.json.ast.JNumber` in `Scala.js` (see `Scala.js` 
section for more info). Note that this does not mean that we can represent all forms of JSON that can be considered valid
(i.e. duplicate/ordered keys for a `JObject`)

## Unsafe AST
Implementation is in `scala.json.unsafe.JValue`

### Goals
- Uses the best performing datastructure's for high performance/low memory usage in construction of a `unsafe.JValue`
    - `unsafe.JArray` stored as an `Array`
    - `unsafe.JObject` stored as an `Array`
    - `unsafe.JNumber` stored as a `String`
- Doesn't use `Scala`'s `stdlib` collection's library
- Defer all runtime errors. We don't throw errors if you happen to provide Infinity/NaN
- Allow duplicate and ordered keys for a `unsafe.JObject`.
- Allow any length/precision of numbers for `unsafe.JNumber` since its represented as a `String`
- This means that `unsafe.JValue` can represent everything that can
can be considered valid under the official [JSON spec](https://www.ietf.org/rfc/rfc4627.txt), even if its not considered sane
- Is referentially transparent in regards to `String` -> `unsafe.JValue` -> `String` since `unsafe.JObject` preserves ordering/duplicate
keys

## Conversion between scala.json.JValue and scala.json.ast.unsafe.JValue

Any `scala.json.ast.JValue` implements a conversion to `scala.json.ast.unsafe.JValue` with a `toUnsafe` method and vice versa with a
`toStandard` method. These conversion methods have been written to be as fast as possible.

There are some peculiarities when converting between the two AST's. When converting a `scala.json.ast.unsafe.JNumber` to a 
`scala.json.ast.JNumber`, it is possible for this to fail at runtime (since the internal representation of 
`scala.json.ast.unsafe.JNumber` is a string). It is up to the caller on how to handle this error (and when), 
a runtime check is deliberately avoided on our end for performance reasons.

Converting from a `scala.json.ast.JObject` to a `scala.json.ast.unsafe.JObject` will produce 
an `scala.json.ast.unsafe.JObject` with an undefined ordering for its internal `Array`/`js.Array` representation.
This is because a `Map` has no predefined ordering. If you wish to provide ordering, you will either need
to write your own custom conversion to handle this case. Duplicate keys will also be removed for the same reason
in an undefined manner.

Do note that according to the JSON spec, whether to order keys for a JObject is not specified. Also note that `Map` 
disregards ordering for equality, however `Array`/`js.Array` equality takes ordering into account.

## .to[T] Conversion

Both `scala.json.ast.JNumber` and `scala.json.ast.unsafe.JNumber` provide conversions using a `.to[T]` method. These methods 
provide a default fast implementations for converting between different number types (as well
as stuff like `Char[Array]`). You can provide your own implementations of a `.to[T]` 
conversion by creating an `implicit val` that implements a JNumberConverter, i.e.

```scala
implicit val myNumberConverter = new JNumberConverter[SomeNumberType]{
  def apply(b: BigDecimal): SomeNumberType = ???
}
```

Then you just need to provide this implementation in scope for usage

## Scala.js
Scala json ast also provides support for [Scala.js](https://github.com/scala-js/scala-js). 
There is even a separate `AST` implementation specifically for `Scala.js` with `@JSExport` for the various `JValue` types, 
which means you are able to construct a `JValue` in `Javascript`in the rare cases that you may need to do so. 
Hence there are added constructors for various `JValue` subtypes, i.e. you can pass in a `Javascript` `array` (i.e. `[]`) 
to construct a `JArray`, as well as a constructor for `JObject` that allows you to pass in a standard `Javascript` 
object with `JValue` as keys (i.e. `{}`).

Examples of constructing various `JValue`'s are given below.

```javascript
var jArray = new scala.json.ast.JArray([new JString("test")]);

var jObject = new scala.json.ast.JObject({"someString" : jArray});

var jObjectWithBool = scala.json.ast.JObject({
    "someString" : jArray,
    "someBool" : scala.json.ast.JTrue()
});

var jObjectWithBoolAndNumber = scala.json.ast.JObject({
    "someString" : jArray,
    "someBool" : scala.json.ast.JTrue(),
    "someNumber" : new scala.json.ast.JNumber(324324.324)
});

var jObjectWithBoolAndNumberAndNull = new scala.json.ast.JObject({
    "someString" : jArray,
    "someBool" : scala.json.ast.JTrue(),
    "someNumber" : new scala.json.ast.JNumber(324324.324),
    "nullValue": scala.json.ast.JNull()
});
```