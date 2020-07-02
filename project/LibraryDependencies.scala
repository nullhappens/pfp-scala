import sbt._

object LibraryDependencies {

  val console4cats = "dev.profunktor" %% "console4cats" % "0.8.1"
  val newtype = "io.estatico" %% "newtype" % "0.4.3"
  val squants = "org.typelevel" %% "squants" % "1.6.0"
  val catsRetry = "com.github.cb372" %% "cats-retry" % "1.1.1"
  val log4CatsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"

  object log4j2 {
    private val log4j2Version = "2.13.3"
    val core = "org.apache.logging.log4j" % "log4j-core" % log4j2Version
    val api = "org.apache.logging.log4j" % "log4j-api" % log4j2Version
    val slf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2Version
  }
  object Fs2 {
    private val fs2Version = "2.2.2"
    val core = "co.fs2" %% "fs2-core" % fs2Version
  }

  object Cats {
    private val catsVersion = "2.1.0"
    val core = "org.typelevel" %% "cats-core" % catsVersion
    val effect = "org.typelevel" %% "cats-effect" % catsVersion
  }

  object Refined {
    private val refinedVersion = "0.9.12"
    val core = "eu.timepit" %% "refined" % refinedVersion
  }

  object Derevo {
    private val derevoVersion = "0.11.4"
    val cats = "org.manatki" %% "derevo-cats" % derevoVersion
    val catsTagless = "org.manatki" %% "derevo-cats-tagless" % derevoVersion
  }

  object Meow {
    private val meowVersion = "0.4.0"
    val core = "com.olegpy" %% "meow-mtl-core" % meowVersion
    val effects = "com.olegpy" %% "meow-mtl-effects" % meowVersion
  }

  object Monocle {
    private val monocleVersion = "2.0.1"
    val core = "com.github.julien-truffaut" %% "monocle-core" % monocleVersion
    val `macro` = "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion
  }

  object Compiler {
    val kindProjector =
      ("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full)
    val contextApplied = "org.augustjune" %% "context-applied" % "0.1.4"
  }

}
