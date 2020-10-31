import cats.effect._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object MainApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit def unsafeLogger = Slf4jLogger.getLogger[IO]

    for {
      _ <- IO(println("it works"))
      _ <- Logger[IO].debug("Is logging working?")
    } yield ExitCode.Success
  }
}
