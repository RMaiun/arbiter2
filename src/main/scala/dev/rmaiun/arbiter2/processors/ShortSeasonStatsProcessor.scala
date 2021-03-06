package dev.rmaiun.arbiter2.processors

import cats.Monad
import dev.rmaiun.arbiter2.commands.SeasonStatsCmd
import dev.rmaiun.arbiter2.dtos.stats.SeasonShortStats
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.helpers.{ ReportCache, StatsCalculator }
import dev.rmaiun.arbiter2.helpers.ReportCache.SeasonReport
import dev.rmaiun.arbiter2.managers.GameManager
import dev.rmaiun.arbiter2.utils.IdGen
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.CmdType.SHORT_STATS_CMD
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import ReportCache.SeasonReport
import dev.rmaiun.arbiter2.helpers.{ ReportCache, StatsCalculator }
import dev.rmaiun.arbiter2.utils.Constants._
import dev.rmaiun.protocol.http.GameDtoSet.ListGameHistoryDtoIn

class ShortSeasonStatsProcessor[F[_]: Monad](gameManager: GameManager[F], cache: ReportCache[F]) extends Processor[F] {
  override def definition: Definition = Definition.query(SHORT_STATS_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    cache.get(SeasonReport).flatMap {
      case Some(v) => Flow.pure(Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, v)))
      case None    => processInternal(input)
    }

  def processInternal(input: BotRequest): Flow[F, Option[ProcessorResponse]] = {
    val action = for {
      dto         <- parseDto[SeasonStatsCmd](input.data)
      historyList <- gameManager.listGameHistory(ListGameHistoryDtoIn(defaultRealm, dto.season))
    } yield {
      val stats = StatsCalculator.calculate(dto.season, historyList.games)
      val msg   = message(stats)
      Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, msg))
    }
    for {
      pr <- action
      _  <- cache.put(SeasonReport, pr.map(_.botResponse.result))
    } yield pr
  }

  private def message(data: SeasonShortStats): String =
    if (data.gamesPlayed == 0) {
      s"No games found for season ${data.season}".toBotMsg
    } else {
      val ratings = if (data.playersRating.isEmpty) {
        "Nobody played more than 20 games"
      } else {
        data.playersRating.zipWithIndex
          .map(e => s"${e._2 + 1}. ${e._1.surname.capitalize} ${e._1.score} %")
          .mkString(LINE_SEPARATOR)
      }
      val bestStreak = data.bestStreak
        .map(s => s"${s.player.capitalize}: ${s.games} in row")
        .getOrElse(DEFAULT_RESULT)
      val worstStreak = data.worstStreak
        .map(s => s"${s.player.capitalize}: ${s.games} in row")
        .getOrElse(DEFAULT_RESULT)
      val separator = "-" * 30
      s"""Season: ${data.season}
         |Games played: ${data.gamesPlayed}
         |Season ends in: ${data.daysToSeasonEnd} days
         |$separator
         |Rating:
         |$ratings
         |${formatUnrankedPlayers(separator, data)}
         |$separator
         |Best streak:
         |$bestStreak
         |$separator
         |Worst streak
         |$worstStreak
         |""".stripMargin.toBotMsg
    }

  private def formatUnrankedPlayers(separator: String, data: SeasonShortStats): String =
    if (data.unrankedStats.isEmpty) {
      DEFAULT_RESULT
    } else {
      val unrankedData = data.unrankedStats
        .map(us => s"- ${us.player.capitalize} +${us.gamesToPlay}")
        .mkString(LINE_SEPARATOR)
      s"""$separator
         |Waiting it rating:
         |$unrankedData""".stripMargin
    }

}

object ShortSeasonStatsProcessor {
  def apply[F[_]](implicit ev: ShortSeasonStatsProcessor[F]): ShortSeasonStatsProcessor[F] = ev
  def impl[F[_]: Monad](gameManager: GameManager[F], cache: ReportCache[F]): ShortSeasonStatsProcessor[F] =
    new ShortSeasonStatsProcessor[F](gameManager, cache)
}
