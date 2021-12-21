package dev.rmaiun.soos

import cats.Monad
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import dev.rmaiun.serverauth.middleware.Arbiter2Middleware
import dev.rmaiun.soos.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.soos.managers.{ GameManager, RealmManager, SeasonManager, UserManager }
import dev.rmaiun.soos.repositories._
import dev.rmaiun.soos.routes.SoosRoutes
import dev.rmaiun.soos.services._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router

object Module {
  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
  )(implicit cfg: ConfigProvider.Config, T: Timer[F], C: ContextShift[F]): HttpApp[F] = {
    lazy val transactor = TransactorProvider.hikariTransactor(cfg)

    lazy val algorithmRepo: AlgorithmRepo[F] = AlgorithmRepo.impl
    lazy val realmRepo                       = RealmRepo.impl
    lazy val roleRepo                        = RoleRepo.impl
    lazy val userRepo                        = UserRepo.impl
    lazy val seasonRepo                      = SeasonRepo.impl
    lazy val gameRepo                        = GameRepo.impl

    lazy val algorithmService  = AlgorithmService.impl(algorithmRepo, transactor)
    lazy val realmService      = RealmService.impl(realmRepo, transactor)
    lazy val roleService       = RoleService.impl(transactor, roleRepo)
    lazy val seasonService     = SeasonService.impl(seasonRepo, realmService, algorithmService, transactor)
    lazy val userService       = UserService.impl(transactor, userRepo)
    lazy val userRightsService = UserRightsService.impl(userService, roleService)
    lazy val gameService       = GameService.impl(gameRepo, transactor)
    //managers
    lazy val realmMng  = RealmManager.impl(realmService, algorithmService, userService)
    lazy val userMng   = UserManager.impl(userService, userRightsService, realmService, roleService, gameService)
    lazy val gameMng   = GameManager.impl(gameService, userService, realmService, seasonService, userRightsService)
    lazy val seasonMng = SeasonManager.impl(algorithmService, realmService, seasonService, cfg.app)
    // http
    val realmHttpApp       = SoosRoutes.realmRoutes[F](realmMng)
    val userHttpApp        = SoosRoutes.userRoutes[F](userMng)
    val seasonHttpApp      = SoosRoutes.seasonRoutes[F](seasonMng)
    val gameHistoryHttpApp = SoosRoutes.gameHistoryRoutes[F](gameMng)
    val eloPointsHttpApp   = SoosRoutes.eloPointsRoutes[F](gameMng)
    val archiveHttpApp     = SoosRoutes.archiveRoutes[F]
    // routes
    val routes = Router[F](
      "/realms"          -> realmHttpApp,
      "/users"           -> userHttpApp,
      "/seasons"         -> seasonHttpApp,
      "/games/history"   -> gameHistoryHttpApp,
      "/games/eloPoints" -> eloPointsHttpApp,
      "/archive"         -> archiveHttpApp
    )
    Arbiter2Middleware(routes, cfg.server.tokens.split(":").toList).orNotFound
  }
}
