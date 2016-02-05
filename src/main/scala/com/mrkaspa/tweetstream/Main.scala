package com.mrkaspa.tweetstream

/**
  * Created by michelperez on 2/2/16.
  */

import java.util.concurrent.{ExecutorService, Executors}

import akka.actor._
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._

import scala.concurrent._

object Main extends App {

  // ActorSystem & thread pools
  val execService: ExecutorService = Executors.newCachedThreadPool()
  implicit val system: ActorSystem = ActorSystem("ciaky")
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(execService)
  implicit val materializer = ActorMaterializer()(system)

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    // create a TwitterStreamClient that pubbish on the event bus, and start its job
    val twitterStream = new TwitterStreamClient(system)
    twitterStream.init

    val theDressHashtag = Hashtag("#TheDress")

    // create a Source, with an actor that listen items from the event bus
    val tweets = Source.actorPublisher[Tweet](Props[StatusPublisherActor])
    val filter = Flow[Tweet].filter(t => t.hashtags.contains(theDressHashtag))
    val bcast = b.add(Broadcast[Tweet](2))
    val mapToTuple = Flow[Tweet].map[(Long, Tweet)](t => (0L, t))
    val whiteGoldFilter = Flow[Tweet].filter(t =>
      t.body.contains("white") || t.body.contains("gold") || t.body.contains("bianco") ||
        t.body.contains("oro") || t.body.contains("blanco") || t.body.contains("dorado"))

    val blueBlackFilter = Flow[Tweet].filter(t =>
      t.body.contains("blue") || t.body.contains("black") || t.body.contains("nero") ||
        t.body.contains("blu") || t.body.contains("negro") || t.body.contains("azul"))

    val sum = Flow[(Long, Tweet)].scan[(Long, Tweet)](0L, EmptyTweet)(
      (state, newValue) => (state._1 + 1L, newValue._2))

    val outWhiteGold = Sink.foreach[(Long, Tweet)]({
      case (count, tweet) => println(count + " white&gold(s). Current tweet: " + tweet.body + " -  " + tweet.author.handle)
    })

    val outBlueBlack = Sink.foreach[(Long, Tweet)]({
      case (count, tweet) => println(count + " blue&black(s). Current tweet: " + tweet.body + " -  " + tweet.author.handle)
    })

    tweets ~> filter ~> bcast ~> whiteGoldFilter ~> mapToTuple ~> sum ~> outWhiteGold
    bcast ~> blueBlackFilter ~> mapToTuple ~> sum ~> outBlueBlack
    ClosedShape
  })

  g.run()
}