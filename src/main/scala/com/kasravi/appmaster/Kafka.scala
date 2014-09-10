package com.kasravi.appmaster

import java.io.File
import akka.persistence.kafka._
import com.typesafe.config._
import kafka.server._
import org.apache.curator.test.TestingServer

object KafkaServerConfig {
  def load(): KafkaServerConfig =
    load("application")

  def load(resource: String): KafkaServerConfig =
    new KafkaServerConfig(ConfigFactory.load(resource).getConfig("test-server"))
}

class KafkaServerConfig(config: Config) {
  object zookeeper {
    val port: Int =
      config.getInt("zookeeper.port")

    val dir: String =
      config.getString("zookeeper.dir")
  }

  val kafka: KafkaConfig =
    new KafkaConfig(configToProperties(config.getConfig("kafka"),
      Map("zookeeper.connect" -> s"localhost:${zookeeper.port}", "host.name" -> "localhost")))
}

class AppMasterKafkaServer(config: KafkaServerConfig = KafkaServerConfig.load()) {
  val zookeeper = new KafkaZookeeperServer(config)
  val kafka = new KafkaServer(config)

  def stop(): Unit = {
    kafka.stop()
    zookeeper.stop()
  }
}

class KafkaZookeeperServer(config: KafkaServerConfig) {
  import config._

  private val server: TestingServer =
    new TestingServer(zookeeper.port, new File(zookeeper.dir))

  def stop(): Unit = server.stop()
}

class KafkaServer(config: KafkaServerConfig) {
  private val server = new kafka.server.KafkaServer(config.kafka)

  server.startup()

  def stop(): Unit = {
    server.shutdown()
    server.awaitShutdown()
  }
}
