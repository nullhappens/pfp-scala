import sbt._
import LibraryDependencies._

ThisBuild / scalafixDependencies += scalafixOrganizeImports

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.3",
    scalacOptions += "-Ymacro-annotations",
    organization := "com.nullhappens",
    name := "practical-fp",
    fork := true,
    addCompilerPlugin(Compiler.kindProjector),
    addCompilerPlugin(Compiler.contextApplied),
    addCommandAlias("cpl", "compile"),
    addCommandAlias("fmt", "; scalafmtSbt; compile:scalafmt"),
    addCommandAlias("check", "; scalafmtSbtCheck; compile:scalafmtCheck"),
    addCommandAlias("lint", "; compile:scalafix --check"),
    addCommandAlias("fix", "; compile:scalafix; test:scalafix"),
    libraryDependencies ++= Seq(
      squants,
      console4cats,
      newtype,
      log4CatsSlf4j,
      catsRetry,
      log4j2.core,
      log4j2.api,
      log4j2.slf4j,
      http4sJwtAuth,
      Http4s.core,
      Http4s.dsl,
      Http4s.blazeServer,
      Http4s.blazeClient,
      Http4s.circe,
      Circe.core,
      Circe.parser,
      Circe.generic,
      Circe.refined,
      Cats.core,
      Cats.effect,
      Derevo.cats,
      Derevo.catsTagless,
      Fs2.core,
      Meow.core,
      Meow.effects,
      Refined.core,
      Monocle.core,
      Monocle.`macro`,
      Skunk.core,
      Skunk.circe,
      Redis4Cats.core,
      Redis4Cats.effects,
      Redis4Cats.streams,
      Redis4Cats.log4cats
    )
  )
