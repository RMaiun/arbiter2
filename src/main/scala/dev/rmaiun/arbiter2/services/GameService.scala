package dev.rmaiun.arbiter2.services

import cats.data.NonEmptyList
import dev.rmaiun.arbiter2.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.arbiter2.db.projections.{ EloPointExtended, EloPointsData, GameHistoryData }
import dev.rmaiun.arbiter2.dtos.{ EloPointsCriteria, GameHistoryCriteria }
import dev.rmaiun.arbiter2.repositories.GameRepo
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait GameService[F[_]] {
  def createEloPoint(ep: EloPoints): Flow[F, EloPoints]
  def createGameHistory(gh: GameHistory): Flow[F, GameHistory]
  def removeNEloPoints(idList: List[Long] = Nil): Flow[F, Int]
  def removeNGameHistory(idList: List[Long] = Nil): Flow[F, Int]
  def listEloPointsByCriteria(surnames: List[String] = Nil): Flow[F, List[EloPointExtended]]
  def listCalculatedPoints(surnames: Option[List[String]] = None): Flow[F, List[EloPointsData]]
  def listHistoryByCriteria(criteria: GameHistoryCriteria): Flow[F, List[GameHistoryData]]
}

object GameService {
  def apply[F[_]](implicit ev: GameService[F]): GameService[F] = ev

  def impl[F[_]: MonadThrowable](gameRepo: GameRepo[F], xa: HikariTransactor[F]): GameService[F] = new GameService[F] {
    override def createEloPoint(ep: EloPoints): Flow[F, EloPoints] =
      gameRepo.createEloPoint(ep).transact(xa).attemptSql.adaptError

    override def createGameHistory(gh: GameHistory): Flow[F, GameHistory] =
      gameRepo.createGameHistory(gh).transact(xa).attemptSql.adaptError

    override def removeNEloPoints(idList: List[Long]): Flow[F, Int] =
      gameRepo.removeNEloPoints(idList).transact(xa).attemptSql.adaptError

    override def removeNGameHistory(idList: List[Long]): Flow[F, Int] =
      gameRepo.removeNGameHistory(idList).transact(xa).attemptSql.adaptError

    override def listEloPointsByCriteria(surnames: List[String]): Flow[F, List[EloPointExtended]] = {
      val criteria = NonEmptyList.fromList(surnames)
      gameRepo.listEloPointsByCriteria(EloPointsCriteria(criteria)).transact(xa).attemptSql.adaptError
    }

    override def listCalculatedPoints(surnames: Option[List[String]]): Flow[F, List[EloPointsData]] = {
      val nonEmptySurnames = surnames.flatMap(NonEmptyList.fromList)
      gameRepo.listCalculatedPoints(nonEmptySurnames).transact(xa).attemptSql.adaptError
    }

    override def listHistoryByCriteria(criteria: GameHistoryCriteria): Flow[F, List[GameHistoryData]] =
      gameRepo.listHistoryByCriteria(criteria).transact(xa).attemptSql.adaptError
  }
}
