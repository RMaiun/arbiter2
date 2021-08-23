package dev.rmaiun.datamanager.dtos.api

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

object RealmDtos {
  case class RegisterRealmDtoIn(realmName: String, algorithm: String, teamSize: Int)
  case class RegisterRealmDtoOut(id: Long, name: String, selectedAlgorithm: String, teamSize: Int)

  case class UpdateRealmAlgorithmDtoIn(id: Long, algorithm: String)
  case class UpdateRealmAlgorithmDtoOut(id: Long, name: String, algorithm: String, teamSize: Int)

  case class DropRealmDtoIn(id: Long)
  case class DropRealmDtoOut(id: Long, removedQty: Int)

  case class GetRealmDtoIn(realm: String)
  case class GetRealmDtoOut(id: Long, name: String, teamSize: Int, selectedAlgorithm: Long)

  object codec {
    implicit val RegisterRealmDtoInEncoder: Encoder[RegisterRealmDtoIn] = deriveEncoder[RegisterRealmDtoIn]
    implicit val RegisterRealmDtoInDecoder: Decoder[RegisterRealmDtoIn] = deriveDecoder[RegisterRealmDtoIn]

    implicit val RegisterRealmDtoOutEncoder: Encoder[RegisterRealmDtoOut] = deriveEncoder[RegisterRealmDtoOut]
    implicit val RegisterRealmDtoOutDecoder: Decoder[RegisterRealmDtoOut] = deriveDecoder[RegisterRealmDtoOut]

    implicit val UpdateRealmAlgorithmDtoInEncoder: Encoder[UpdateRealmAlgorithmDtoIn] =
      deriveEncoder[UpdateRealmAlgorithmDtoIn]
    implicit val UpdateRealmAlgorithmDtoInDecoder: Decoder[UpdateRealmAlgorithmDtoIn] =
      deriveDecoder[UpdateRealmAlgorithmDtoIn]

    implicit val UpdateRealmAlgorithmDtoOutEncoder: Encoder[UpdateRealmAlgorithmDtoOut] =
      deriveEncoder[UpdateRealmAlgorithmDtoOut]
    implicit val UpdateRealmAlgorithmDtoOutDecoder: Decoder[UpdateRealmAlgorithmDtoOut] =
      deriveDecoder[UpdateRealmAlgorithmDtoOut]

    implicit val DropRealmDtoInEncoder: Encoder[DropRealmDtoIn] = deriveEncoder[DropRealmDtoIn]
    implicit val DropRealmDtoInDecoder: Decoder[DropRealmDtoIn] = deriveDecoder[DropRealmDtoIn]

    implicit val DropRealmDtoOutEncoder: Encoder[DropRealmDtoOut] = deriveEncoder[DropRealmDtoOut]
    implicit val DropRealmDtoOutDecoder: Decoder[DropRealmDtoOut] = deriveDecoder[DropRealmDtoOut]

    implicit val GetRealmDtoInEncoder: Encoder[GetRealmDtoIn] = deriveEncoder[GetRealmDtoIn]
    implicit val GetRealmDtoInDecoder: Decoder[GetRealmDtoIn] = deriveDecoder[GetRealmDtoIn]

    implicit val GetRealmDtoOutEncoder: Encoder[GetRealmDtoOut] = deriveEncoder[GetRealmDtoOut]
    implicit val GetRealmDtoOutDecoder: Decoder[GetRealmDtoOut] = deriveDecoder[GetRealmDtoOut]
  }
}