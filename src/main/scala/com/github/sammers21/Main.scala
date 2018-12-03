package com.github.sammers21

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString
import com.github.sammers21.mtproto.MTProtoRequest

import scala.concurrent.Future

/**
  * Entry point.
  */
object Main extends App {

  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind("0.0.0.0", 8888)

  def MTProtoFramer: Flow[ByteString, MTProtoRequest, NotUsed] = {
    ???
  }

  connections.runForeach { connection =>
    val handler = Flow[ByteString]
      .via(MTProtoFramer)
      .map(_.generateResponse)
      .map(_.toByteString)
    connection.handleWith(handler)
  }
}
