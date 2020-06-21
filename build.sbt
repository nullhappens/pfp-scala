import sbt._
import LibraryDependencies._

lazy val root = project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.0",
      scalacOptions += "-Ymacro-annotations",
      name := "practical-fp",
      fork := true,
      addCompilerPlugin(Compiler.kindProjector),
      addCompilerPlugin(Compiler.contextApplied),
      libraryDependencies ++= Seq(
        Cats.core,
        Cats.effect,
        console4cats,
        Derevo.cats,
        Derevo.catsTagless,
        Fs2.core,
        Meow.core,
        Meow.effects,
        newtype,
        Refined.core,
        Monocle.core,
        Monocle.`macro`
    )
  )
