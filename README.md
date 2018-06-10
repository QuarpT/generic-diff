# Generic Diff

Generic Diff is a Scala library providing customisable diff typeclasses for comparing
case classes, coproducts (sealed trait algebras), recursive algebras, sequences,
sets and primitives types.

The library also provides a customisable diff-print typeclass for customising
the diff output format.

### Ordered / Unordered Comparison

The library provides ordered and unordered versions of the Diff typeclass affecting how
iterables are compared.
