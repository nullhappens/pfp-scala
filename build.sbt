import sbt._
import LibraryDependencies._

lazy val root = project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.0",
      scalacOptions += "-Ymacro-annotations",
      organization := "com.nullhappens",
      name := "practical-fp",
      fork := true,
      addCompilerPlugin(Compiler.kindProjector),
      addCompilerPlugin(Compiler.contextApplied),
      libraryDependencies ++= Seq(
        squants,
        console4cats,
        newtype,
        log4CatsSlf4j,
        catsRetry,
        log4j2.core,
        log4j2.api,
        log4j2.slf4j,
        Cats.core,
        Cats.effect,
        Derevo.cats,
        Derevo.catsTagless,
        Fs2.core,
        Meow.core,
        Meow.effects,
        Refined.core,
        Monocle.core,
        Monocle.`macro`
    )
  )
