import cats.implicits._
import cats.effect._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.Logger

object MainApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit def unsafeLogger = Slf4jLogger.getLogger[IO]

    for {
      _ <- IO(println("it works"))
      _ <- Logger[IO].debug("Is logging working?")
    } yield ExitCode.Success
  }
}
