package dev.rmaiun.mabel.postprocessor

import cats.syntax.foldable._
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.mabel.commands.AddRoundCmd
import dev.rmaiun.mabel.dtos.CmdType.ADD_ROUND_CMD
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, Definition }
import dev.rmaiun.mabel.services.{ ArbiterClient, PublisherProxy }
import dev.rmaiun.mabel.utils.Constants._
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.UserDtoSet.UserRoleData

case class AddRoundPostProcessor[F[_]: MonadThrowable](
  arbiterClient: ArbiterClient[F],
  cmdPublisher: PublisherProxy[F]
) extends PostProcessor[F] {

  override def definition: Definition = Definition.internal(ADD_ROUND_CMD)

  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddRoundCmd](input.data)
      _   <- sendNotificationToUser(cmd.w1, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.w2, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.l1, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
      _   <- sendNotificationToUser(cmd.l2, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
      _   <- notifyRealmAdmins(cmd)
    } yield ()

  private def sendNotificationToUser(player: String, opponents: String, win: Boolean): Flow[F, Unit] =
    for {
      userDto    <- arbiterClient.findPlayerBySurname(player.toLowerCase)
      botResponse = BotResponse(userDto.user.tid.getOrElse(-1), IdGen.msgId, createOutput(opponents, win))
      _          <- cmdPublisher.publishToBot(botResponse)(userDto.user.active && botResponse.chatId > 0)
    } yield ()

  private def notifyRealmAdmins(cmd: AddRoundCmd): Flow[F, Unit] =
    for {
      adminsDto <- arbiterClient.findRealmAdmins()
      _         <- sendMsgToAdmins(cmd, adminsWithoutModeratorAndPlayers(cmd, adminsDto.adminUsers))
    } yield ()

  private def sendMsgToAdmins(cmd: AddRoundCmd, admins: List[UserRoleData]): Flow[F, Unit] = {
    val shutout = if (cmd.shutout) "(✓)" else ""
    val msg =
      s"""Round 
         |${cmd.w1.capitalize}/${cmd.w2.capitalize} vs ${cmd.l1.capitalize}/${cmd.l2.capitalize} $shutout
         |was stored
         """.stripMargin.toBotMsg
    admins
      .map(d => BotResponse(d.tid.getOrElse(0), IdGen.msgId, msg))
      .map(response => cmdPublisher.publishToBot(response))
      .sequence_
  }

  private def adminsWithoutModeratorAndPlayers(cmd: AddRoundCmd, data: List[UserRoleData]): List[UserRoleData] = {
    val affectedPlayers = Set(cmd.w1.toLowerCase, cmd.w2.toLowerCase, cmd.l1.toLowerCase, cmd.l2.toLowerCase)
    data
      .filter(urd => urd.tid.fold(false)(tid => tid != cmd.moderator))
      .filter(urd => !affectedPlayers.contains(urd.surname.toLowerCase))
  }

  private def createOutput(opponents: String, win: Boolean): String = {
    val action = if (win) "win" else "lose"
    s"Your $action against $opponents was successfully stored".toBotMsg
  }
}
object AddRoundPostProcessor {
  def apply[F[_]](implicit ev: AddRoundPostProcessor[F]): AddRoundPostProcessor[F] = ev
  def impl[F[_]: MonadThrowable](
    ac: ArbiterClient[F],
    cmdPublisher: PublisherProxy[F]
  ): AddRoundPostProcessor[F] =
    new AddRoundPostProcessor[F](ac, cmdPublisher)
}
