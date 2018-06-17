package com.tailrec.typeclass.examples

import java.time.temporal.ChronoUnit
import java.time.Instant
import com.tailrec.typeclass._

case class PrivateInformation(firstName: String, lastName: String, lastActive: Option[Instant])
case class User(username: String, level: Int, privateInformation: PrivateInformation)

trait DiffExample extends DiffImplicits {

  // In this example we diff two user objects with a different `level` and `lastActive` date

  val now: Instant = Instant.now()
  val nowSeconds: Instant = now.truncatedTo(ChronoUnit.SECONDS)

  val user1 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(now)))
  val user2 = User("player1", 4, PrivateInformation("Jess", "Smith", Some(nowSeconds)))

  // Output:
  //
  // Difference at /level
  //     5 not equals 4
  // Difference at /privateInformation/lastActive/value
  //     2018-06-10T17:30:18.323Z not equals 2018-06-10T17:30:18Z
  println(Diff(user1, user2).description)
}

trait CustomDiffExample extends DiffImplicits {

  // We provide a custom Instant diff for comparing the last active date ignoring the milliseconds
  // resulting in the Identical result.

  implicit val instantDiff: Diff[Instant] = Diff.build { (left, right) =>
    if (left.truncatedTo(ChronoUnit.SECONDS) == right.truncatedTo(ChronoUnit.SECONDS))
      Identical
    else
      Different.fromPair(left, right)
  }

  val now: Instant = Instant.now()
  val nowSeconds: Instant = now.truncatedTo(ChronoUnit.SECONDS)

  val user1 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(now)))
  val user2 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(nowSeconds)))

  // Always returns Identical using the custom instantDiff typeclass to compare Instants
  //
  // Output:
  //
  // Identical
  println(Diff(user1, user2).description)
}

trait CustomFormattingExample extends DiffImplicits {

  // In this scenario we provide a custom DiffPrint typeclass when comparing two users with different lastActive dates.

  implicit val instantPrinter: DiffPrint[Instant] = DiffPrint.build(_.toEpochMilli.toString)

  val now: Instant = Instant.now()
  val nowSeconds: Instant = now.truncatedTo(ChronoUnit.SECONDS)

  val user1 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(now)))
  val user2 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(nowSeconds)))

  // The different description uses the custom numeric printer provided by the instantPrinter typeclass
  //
  // Output:
  //
  // Difference at /privateInformation/lastActive/value
  //     1528652133731 not equals 1528652133000
  println(Diff(user1, user2).description)
}


trait CustomSuperTypeDiffExample extends DiffImplicits {

  // In this example we diff two user objects which are identical except for the lastActive date Option
  // We provide a custom Option diff typeclass for comparing options where we treat None and Some as identical.
  // Otherwise we use the implicit Diff[A] for the (Some,Some) case

  // It's best to use the <:< evidence type constructor, to ensure the custom typeclass overrides the default diff typeclass

  implicit def optionDiff[A, B](implicit ev: B <:< Option[A],
                                diff: Diff[A]): Diff[B] = Diff.build {
    case (Some(left), Some(right)) => Different.fromPair(left, right)
    case _ => Identical
  }

  val now: Instant = Instant.now()

  val user1 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(now)))
  val user2 = User("player1", 5, PrivateInformation("Jess", "Smith", None))

  // Always returns Identical using the custom Option diff typeclass
  //
  // Output:
  //
  // Identical
  println(Diff(user1, user2).description)
}

trait OrderedSequenceComparison extends DiffImplicits {

  // In this example we diff two lists

  val l1 = List(1, 2, 3)
  val l2 = List(3, 2, 1)

  // The output describes a difference found at index 0 and index 2 of the lists
  //
  // Output:
  //
  // Difference at /[0]
  //      1 not equals 3
  //  Difference at /[2]
  //      3 not equals 1
  println(Diff(l1, l2).description)
}

trait UnorderedSequenceComparison extends UnorderedDiffImplicits {

  // In this example we diff two lists using the unordered diff typeclass

  val l1 = List(1, 2, 3)
  val l2 = List(3, 2, 1)

  // The output describes a difference found at index 0 and index 2 of the lists
  //
  // Output:
  //
  // Identical
  println(Diff(l1, l2).description)
}

object Main extends App with UnorderedSequenceComparison
