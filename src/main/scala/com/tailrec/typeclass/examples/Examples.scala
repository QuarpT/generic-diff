package com.tailrec.typeclass.examples

import java.time.temporal.ChronoUnit
import java.time.Instant
import com.tailrec.typeclass._

case class PrivateInformation(firstName: String, lastName: String, lastActive: Option[Instant])
case class User(username: String, level: Int, privateInformation: PrivateInformation)

trait DiffExample extends DiffImplicits {

  // In this example we diff two user objects with a different level and lastActive date

  val now: Instant = Instant.now()
  val nowSeconds: Instant = now.truncatedTo(ChronoUnit.SECONDS)

  val user1 = User("player1", 5, PrivateInformation("Jess", "Smith", Some(now)))
  val user2 = User("player1", 4, PrivateInformation("Jess", "Smith", Some(nowSeconds)))

  // Compare two objects identical except for an internal instant with
  // If the instants are different it will output the path and the comparison:
  //
  // Output:
  //
  // Difference at /level
  //   5 not equals 4
  // Difference at /privateInformation/lastActive/value
  //   2018-06-10T17:30:18.323Z not equals 2018-06-10T17:30:18Z
  println(Diff(user1, user2).description)
}

trait CustomDiffExample extends DiffImplicits {

  // In this example we diff two user objects which are identical except for the lastActive date
  // We provide a custom Instant diff for comparing the last active date, resulting in the Identical result.

  implicit val instantDiff: Diff[Instant] = Diff.build { (left, right) =>
    if (math.abs(left.toEpochMilli - right.toEpochMilli) < 1000) Identical else Different.fromPair(left, right)
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

  // In this scenario we provide a custom DiffPrint typeclass when comparing two users with different lastActiveDates.

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


trait CustomRecursiveDiffExample extends DiffImplicits {

  // In this example we diff two user objects which are identical except for the lastActive date Option
  // We provide a custom Option diff typeclass for comparing options where we treat None and Some as identical.
  // Otherwise we use the implicit Diff[A] for the (Some,Some) case

  implicit def optionDiff[A](implicit diff: Diff[A]): Diff[Option[A]] = Diff.build {
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
