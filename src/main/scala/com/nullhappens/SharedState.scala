package com.nullhappens

import scala.concurrent.duration._

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

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
