package com.nullhappens

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.effect.implicits._
import cats.implicits._
import scala.concurrent.duration._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.Logger

object SharedState extends IOApp {

  implicit val unsafeLogger = Slf4jLogger.getLogger[IO]

  def someExpensiveTask: IO[Unit] =
    IO.sleep(1.second) >>
      Logger[IO].debug("expensive task") >>
      someExpensiveTask

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p1(sem)

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p2(sem)

  override def run(args: List[String]): IO[ExitCode] =
    Semaphore[IO](1).flatMap { sem =>
      p1(sem).start.void *>
      p2(sem).start.void
    } *> IO.never.as(ExitCode.Success)

}
