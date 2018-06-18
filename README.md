# Generic Diff

Generic Diff is a Scala library providing customisable diff typeclasses for comparing
case classes, coproducts (sealed trait algebras), recursive algebras, sequences,
sets and primitives types.

This is useful for writing experiments and tests which compare complex data structures

The library also provides 
- a customisable typeclass for controlling the diff output format
- a typeclass for retrieving class type names, used internally

The library implementation is compile time only. No runtime reflection is used.

Powered by shapeless

## Example test use case

When testing a data structure against the expected value, you want to ignore comparing a 
non deterministic nested property such as a Timestamp field. The library allows you to 
provide a custom Timestamp diff implementation of the typeclass allowing your comparison
to succeed. This is superior to testing against a small subset of properies on the data structure.

## Install

For Scala 2.12
```
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.QuarpT" %% "generic-diff" % "v2.0.0"
```

For Scala 2.11
```
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.QuarpT" %% "generic-diff" % "v1.0.0"
```

## Examples

Detailed examples in [Examples.scala](src/main/scala/com/tailrec/typeclass/examples/Examples.scala)

### Basic usage

mixin `com.tailrec.typeclass.DiffImplicits`

```
import com.tailrec.typeclass._

object DiffExample extends DiffImplicits {
  ...
}
```

Diff case classes

```
case class PrivateInformation(firstName: String, lastName: String, lastActive: Option[Instant])
case class User(username: String, level: Int, privateInformation: PrivateInformation)
```

Output example:

```
println(Diff[User](user1, user2))
// Difference at /level
//     5 not equals 4
// Difference at /privateInformation/lastActive/value
//     2018-06-10T17:30:18.323Z not equals 2018-06-10T17:30:18Z
```

### Custom type comparison

Write a custom Instant time Diff typeclass which ignore milliseconds

This would change the way `lastActive` in `User` is compared

```
implicit val instantDiff: Diff[Instant] = Diff.build { (left, right) =>
  if (left.truncatedTo(ChronoUnit.SECONDS) == right.truncatedTo(ChronoUnit.SECONDS))
    Identical
  else
    Different.fromPair(left, right)
}
```

### Custom super type comparison

Treating `Some` and `None` as identical for options

It's best to use the `<:<` evidence type constructor to ensure the custom typeclass overrides the default diff typeclass

```
implicit def optionDiff[A, B](implicit ev: B <:< Option[A],
                              diff: Diff[A]): Diff[B] = Diff.build {
  case (Some(left), Some(right)) => Different.fromPair(left, right)
  case _ => Identical
}
```

### Custom class comparison

This is only necessary for non case classes - case class comparison is provided by the library.

We construct the `DiffResult` explicitly using the `+` operator while prepending the field names to the diff namespace.
DiffResults are monoids

```
class NonCaseClassUser(val username: String, val level: Int, val privateInformation: PrivateInformation)

implicit def nonCaseClassUserDiff(implicit stringDiff: Diff[String],
                                  intDiff: Diff[Int],
                                  privateInformationDiff: Diff[PrivateInformation]): Diff[NonCaseClassUser] =  
  Diff.build {
    (left, right) =>
      stringDiff(left.username, right.username).prependNamespace("username") +
        intDiff(left.level, right.level).prependNamespace("level") +
        privateInformationDiff(left.privateInformation, right.privateInformation).prependNamespace("privateInformation")
  }
 ```

Example output

```
println(Diff[NonCaseClassUser](user1, user2))
// Difference at /level
//     5 not equals 4
// Difference at /privateInformation/lastActive/value
//     2018-06-10T17:30:18.323Z not equals 2018-06-10T17:30:18Z
```

### Custom diff formatting

Format Instants as milliseconds
```
implicit val instantPrinter: DiffPrint[Instant] = DiffPrint.build(_.toEpochMilli.toString)
```

```
println(Diff(user1, user2))
// Difference at /privateInformation/lastActive/value
//     1528652133731 not equals 1528652133000
```
### Ordered / Unordered iterable Comparison

The library provides ordered and unordered versions of the Diff typeclass affecting how
iterables are compared.

```
object DiffExamples extends UnorderedDiffImplicits {
  ...
}
```


