package com.mrkaspa.tweetstream

import akka.stream.actor.ActorPublisher

/**
  * Created by michelperez on 2/2/16.
  */
class StatusPublisherActor extends ActorPublisher[Tweet] {

  val sub = context.system.eventStream.subscribe(self, classOf[Tweet])

  override def receive: Receive = {
    case s: Tweet => {
      println(s"Trino recibido: ${s.body}")
      if (isActive && totalDemand > 0) onNext(s)
    }
    case _ =>
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }

}