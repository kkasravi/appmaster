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
    case "snap" =>
      saveSnapshot(state)
    case SaveSnapshotSuccess(md) =>
      println(s"snapshot saved (metadata = ${md})")
    case SaveSnapshotFailure(md, e) =>
      println(s"snapshot saving failed (metadata = ${md}, error = ${e.getMessage})")
    case PersistenceFailure(payload, snr, e) =>
      println(s"persistence failed (payload = ${payload}, sequenceNr = ${snr}, error = ${e.getMessage})")
  }

  def receiveRecover: Receive = {
    case i: Increment =>
      update(i)
    case SnapshotOffer(md, snapshot: Int) =>
      state = snapshot
      println(s"state initialized: ${state} (metadata = ${md})")
    case RecoveryFailure(e) =>
      println(s"recovery failed (error = ${e.getMessage})")
  }

  def update(i: Increment): Unit = {
    state += i.value
    println(s"state updated: ${state} (last sequence nr = ${lastSequenceNr})")
  }
}

class AppMasterEventTopicMapper extends EventTopicMapper {
  def topicsFor(event: Event): Seq[String] = event.persistenceId match {
    case "a" => List("topic-a-1", "topic-a-2")
    case "b" => List("topic-b")
    case _   => Nil
  }
}

object AppMaster extends App {
  case class Increment(value: Int)

  val kafkaServer = new AppMasterKafkaServer(KafkaServerConfig.load("appmaster"))
  val system = ActorSystem("appmaster", ConfigFactory.load("appmaster"))
  val actorA = system.actorOf(Props(new AppMasterProcessor("a")))

  actorA ! Increment(2)
  actorA ! Increment(3)
  actorA ! "snap"
}
