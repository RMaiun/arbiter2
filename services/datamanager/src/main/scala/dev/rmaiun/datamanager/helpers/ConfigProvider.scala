package dev.rmaiun.datamanager.helpers

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class DbConfig(host: String, port: Int, database: String, username: String, password: String)

  case class ServerConfig(port: Int)

  case class Config(db: DbConfig, server: ServerConfig)

  def provideConfig: Config =
    ConfigSource.default.loadOrThrow[Config]
}