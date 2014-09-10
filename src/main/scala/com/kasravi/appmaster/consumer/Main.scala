package com.kasravi.appmaster.consumer

import java.util.Properties
import akka.persistence.kafka.{DefaultEventDecoder, Event}
import kafka.consumer.{Consumer, ConsumerConfig}
import kafka.serializer.{DefaultDecoder, StringDecoder}

object Main extends App {
  val props = new Properties()
  props.put("group.id", "consumer-1")
  props.put("zookeeper.connect", "localhost:2181")
  props.put("auto.offset.reset", "smallest")
  props.put("auto.commit.enable", "false")

  val consConn = Consumer.create(new ConsumerConfig(props))
  val streams = consConn.createMessageStreams(Map("topic-a" -> 1),
    keyDecoder = new StringDecoder, valueDecoder = new DefaultEventDecoder)

  streams("topic-a")(0).foreach { mm =>
    val event: Event = mm.message
    println(s"consumed ${event}")
  }
}
