# Generic Diff

Generic Diff is a Scala library providing a customisable diff typeclasses for
case classes, coproducts (sealed trait algebras), recursive algebras, sequences,
sets and primitives.

The library also provides a customisable diff-print typeclass for customising
the diff output format.

### Ordered / Unordered Comparison

The library provides ordered and unordered versions of the Diff typeclass.
This affects how the library compares Iterables.
