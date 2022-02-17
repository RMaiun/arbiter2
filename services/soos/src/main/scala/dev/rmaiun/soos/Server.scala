package dev.rmaiun.soos

import cats.effect.{ Async, Clock, Sync }
import cron4s.{ Cron, CronExpr }
import dev.rmaiun.soos.helpers.{ ConfigProvider, DumpExporter }
import eu.timepit.fs2cron.Scheduler
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.concurrent.Executors
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

object Server {
  implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  implicit val cfg: ConfigProvider.Config       = ConfigProvider.provideConfig

  def dumpCronEvaluation[F[_]: Async](exporter: DumpExporter[F])(implicit T: Clock[F]): Stream[F, Unit] = {
    val cronScheduler: Scheduler[F, CronExpr] = Cron4sScheduler.systemDefault[F]
    val cronTick                              = Cron.unsafeParse("0 0 20 ? * *")
    cronScheduler
      .awakeEvery(cronTick)
      .evalTap(_ => exporter.exportDump().value)
  }

  def stream[F[_]: Async](implicit T: Clock[F]): Stream[F, Nothing] = {
    for {
      //general
      client <- BlazeClientBuilder[F].withMaxWaitQueueLimit(1000).stream
      module  = Program.initHttpApp()
      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(module.httpApp)
      exitCode <- BlazeServerBuilder[F]
                    .bindHttp(cfg.server.port, cfg.server.host)
                    .withHttpApp(finalHttpApp)
                    .serve
                    .concurrently(dumpCronEvaluation(module.dumpExporter))
    } yield exitCode
  }.drain
}
