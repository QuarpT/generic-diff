# Generic Diff

Generic Diff is a Scala library providing customisable diff typeclasses for comparing
case classes, coproducts (sealed trait algebras), recursive algebras, sequences,
sets and primitives types.

This is useful for writing experiments and tests which compare complex data structures

The library also provides a customisable diff-print typeclass for customising
the diff output format.

Powered by shapeless

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

```
val diffResult = Diff[User](user1, user2)
println(diffResult)
```

Output example:

```
Difference at /level
    5 not equals 4
Difference at /privateInformation/lastActive/value
    2018-06-10T17:30:18.323Z not equals 2018-06-10T17:30:18Z
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


