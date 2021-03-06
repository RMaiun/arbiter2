package dev.rmaiun.arbiter2.commands

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class LastGamesCmd(season: Option[String] = None)
object LastGamesCmd {
  implicit val LastGamesCmdCodec: Codec[LastGamesCmd] = deriveCodec[LastGamesCmd]
}
