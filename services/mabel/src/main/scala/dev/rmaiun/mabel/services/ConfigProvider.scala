package dev.rmaiun.mabel.services

import pureconfig.ConfigSource

object ConfigProvider {

  case class BrokerConfig(host: String, virtualHost: String, port: Int, username: String, password: String)

  case class ServerConfig(broker: BrokerConfig, host: String, port: Int)

  def provideConfig: ServerConfig =
    ConfigSource.default.loadOrThrow[ServerConfig]
}
