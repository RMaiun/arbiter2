package dev.rmaiun.arbiter2.commands

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class SeasonStatsCmd(season: String)

object SeasonStatsCmd {
  implicit val SeasonStatsCmdCodec: Codec[SeasonStatsCmd] = deriveCodec[SeasonStatsCmd]
}
