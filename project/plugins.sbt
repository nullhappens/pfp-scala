
ivyLoggingLevel := UpdateLogging.Quiet
scalacOptions in Compile ++= Seq("-feature", "-deprecation")

classpathTypes += "maven-plugin"

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.11")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.0")
