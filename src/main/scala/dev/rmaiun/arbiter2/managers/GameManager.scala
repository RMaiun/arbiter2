package dev.rmaiun.arbiter2.managers

import cats.Monad
import dev.rmaiun.arbiter2.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.arbiter2.dtos.GameHistoryCriteria
import dev.rmaiun.arbiter2.services.{ GameService, RealmService, SeasonService, UserRightsService, UserService }
import dev.rmaiun.common.DateFormatter
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.arbiter2.services._
import dev.rmaiun.arbiter2.validations.GameValidationSet._
import dev.rmaiun.protocol.http.GameDtoSet._
import dev.rmaiun.validation.Validator

trait GameManager[F[_]] {
  def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut]
  def listGameHistory(dtoIn: ListGameHistoryDtoIn): Flow[F, ListGameHistoryDtoOut]
  def addEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut]
  def listCalculatedEloPoints(dtoIn: ListEloPointsDtoIn): Flow[F, ListEloPointsDtoOut]
}
object GameManager {
  def apply[F[_]](implicit ev: GameManager[F]): GameManager[F] = ev

  def impl[F[_]: Monad](
    gameService: GameService[F],
    userService: UserService[F],
    realmService: RealmService[F],
    seasonService: SeasonService[F],
    userRightsService: UserRightsService[F]
  ): GameManager[F] = new GameManager[F] {
    override def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut] =
      for {
        _      <- Validator.validateDto[F, AddGameHistoryDtoIn](dtoIn)
        users   = List(dtoIn.historyElement.w1, dtoIn.historyElement.w2, dtoIn.historyElement.l1, dtoIn.historyElement.l2)
        _      <- seasonService.checkAllUsersAreDifferent(users)
        _      <- userRightsService.checkUserWritePermissions(dtoIn.historyElement.realm, dtoIn.moderatorTid)
        realm  <- realmService.getByName(dtoIn.historyElement.realm)
        season <- seasonService.findSeason(dtoIn.historyElement.season, realm)
        w1     <- userService.findByInputType(Some(dtoIn.historyElement.w1.toLowerCase))
        w2     <- userService.findByInputType(Some(dtoIn.historyElement.w2.toLowerCase))
        l1     <- userService.findByInputType(Some(dtoIn.historyElement.l1.toLowerCase))
        l2     <- userService.findByInputType(Some(dtoIn.historyElement.l2.toLowerCase))
        gh = GameHistory(
               0,
               realm.id,
               season.id,
               w1.id,
               w2.id,
               l1.id,
               l2.id,
               dtoIn.historyElement.shutout,
               dtoIn.historyElement.created.getOrElse(DateFormatter.now)
             )
        created <- gameService.createGameHistory(gh)
      } yield AddGameHistoryDtoOut(
        GameHistoryDto(
          created.id,
          realm.name,
          season.name,
          w1.surname,
          w2.surname,
          l1.surname,
          l2.surname,
          created.shutout
        )
      )

    override def listGameHistory(dtoIn: ListGameHistoryDtoIn): Flow[F, ListGameHistoryDtoOut] =
      for {
        _    <- Validator.validateDto[F, ListGameHistoryDtoIn](dtoIn)
        data <- gameService.listHistoryByCriteria(GameHistoryCriteria(dtoIn.realm, Some(dtoIn.season)))
      } yield {
        val dtoList = data.map(gh =>
          StoredGameHistoryDto(
            dtoIn.realm,
            dtoIn.season,
            gh.winner1,
            gh.winner2,
            gh.loser1,
            gh.loser2,
            gh.shutout,
            gh.createdAt
          )
        )
        ListGameHistoryDtoOut(dtoList)
      }

    override def addEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut] =
      for {
        _         <- Validator.validateDto[F, AddEloPointsDtoIn](dtoIn)
        _         <- userRightsService.checkUserWritePermissions(dtoIn.realm, dtoIn.moderatorTid)
        user      <- userService.findByInputType(Some(dtoIn.points.user))
        eloPoints <- gameService.createEloPoint(EloPoints(0, user.id, dtoIn.points.value, DateFormatter.now))
      } yield AddEloPointsDtoOut(eloPoints.id)

    override def listCalculatedEloPoints(dtoIn: ListEloPointsDtoIn): Flow[F, ListEloPointsDtoOut] =
      for {
        _             <- Validator.validateDto[F, ListEloPointsDtoIn](dtoIn)
        users          = dtoIn.users.map(list => list.map(_.toLowerCase))
        eloPointsList <- gameService.listCalculatedPoints(users)
      } yield {
        val dtoList    = eloPointsList.map(ep => CalculatedEloPointsDto(ep.user, ep.points, ep.gamesPlayed))
        val foundUsers = dtoList.map(_.user)
        val missedData = dtoIn.users
          .fold(List.empty[String])(x => x.map(_.toLowerCase()))
          .filter(u => !foundUsers.contains(u))
        ListEloPointsDtoOut(dtoList, missedData)
      }
  }
}
