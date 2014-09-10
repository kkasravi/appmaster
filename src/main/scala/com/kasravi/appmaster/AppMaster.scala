package com.kasravi.appmaster

import scala.collection.immutable.Seq

import akka.actor._
import akka.persistence._
import akka.persistence.kafka.{DefaultEventDecoder, Event, EventTopicMapper}
import com.typesafe.config.ConfigFactory
 
class AppMasterProcessor(val persistenceId: String) extends PersistentActor {
  import AppMaster.Increment

  var state: Int = 0
  def receiveCommand: Receive = {
    case i: Increment =>
      persist(i)(update)
    case PersistenceFailure(payload, snr, e) =>
      println(s"persistence failed (payload = ${payload}, sequenceNr = ${snr}, error = ${e.getMessage})")
    case _ =>
      println("unknown command")
  }

  def receiveRecover: Receive = {
    case i: Increment =>
      update(i)
    case RecoveryFailure(e) =>
      println(s"recovery failed (error = ${e.getMessage})")
    case _ =>
      println("unknown recover")
  }

  def update(i: Increment): Unit = {
    state += i.value
    println(s"state updated: ${state} (last sequence nr = ${lastSequenceNr})")
  }
}

class AppMasterEventTopicMapper extends EventTopicMapper {
  def topicsFor(event: Event): Seq[String] = event.persistenceId match {
    case topic:String => List(s"topic-${topic}")
    case _   => Nil
  }
}

object AppMaster extends App {
  //Data types AppMasterProcessor needs to handle
  case class Increment(value: Int)

  val kafkaServer = new AppMasterKafkaServer(KafkaServerConfig.load("appmaster"))
  val system = ActorSystem("appmaster", ConfigFactory.load("appmaster"))
  val actorA = system.actorOf(Props(new AppMasterProcessor("a")))

  actorA ! Increment(2)
  actorA ! Increment(3)
}
