package com.nullhappens

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object SecondMainApp extends IOApp {

  implicit val unsafeLogger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    Logger[IO].debug("is this working?") >>
      Logger[IO].debug("and this one is working too") >>
      Logger[IO].debug("{ Hello } World") >>
      IO {
        (0 to 100).map { x =>
          x + 1
        }
      } >>
      IO(ExitCode.Success)
}
