package io.github.maoertel.sangriaserver

import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends IOApp {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  def run(args: List[String]): IO[ExitCode] =
    for {
      env <- Configuration(application = "application.conf", db = "db.conf").getEnvironment
      server <- Server.stream(env).compile.drain.as(ExitCode.Success)
    } yield server
}
