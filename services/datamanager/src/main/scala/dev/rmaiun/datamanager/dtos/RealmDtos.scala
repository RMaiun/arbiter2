package dev.rmaiun.datamanager.dtos

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

object RealmDtos {
  case class RegisterRealmDtoIn(realmName: String, algorithm: String, teamSize: Int)
  case class RegisterRealmDtoOut(id: Long, name: String, selectedAlgorithm: String, teamSize: Int)

  case class UpdateRealmAlgorithmDtoIn(id: Long, algorithm: String)
  case class UpdateRealmAlgorithmDtoOut(id: Long, name: String, algorithm: String, teamSize: Int)

  case class DropRealmDtoIn(id: Long)
  case class DropRealmDtoOut(id: Long, removedQty:Int)

  object codec {
    implicit val RegisterRealmDtoInEncoder: Encoder[RegisterRealmDtoIn] = deriveEncoder[RegisterRealmDtoIn]
    implicit val RegisterRealmDtoInDecoder: Decoder[RegisterRealmDtoIn] = deriveDecoder[RegisterRealmDtoIn]

    implicit val RegisterRealmDtoOutEncoder: Encoder[RegisterRealmDtoOut] = deriveEncoder[RegisterRealmDtoOut]
    implicit val RegisterRealmDtoOutDecoder: Decoder[RegisterRealmDtoOut] = deriveDecoder[RegisterRealmDtoOut]

    implicit val DropRealmDtoInEncoder: Encoder[DropRealmDtoIn] = deriveEncoder[DropRealmDtoIn]
    implicit val DropRealmDtoInDecoder: Decoder[DropRealmDtoIn] = deriveDecoder[DropRealmDtoIn]

    implicit val DropRealmDtoOutEncoder: Encoder[DropRealmDtoOut] = deriveEncoder[DropRealmDtoOut]
    implicit val DropRealmDtoOutDecoder: Decoder[DropRealmDtoOut] = deriveDecoder[DropRealmDtoOut]
  }
}
