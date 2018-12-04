package com.github.sammers21

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString
import com.github.sammers21.mtproto.Framing.MTProtoFramer
import com.github.sammers21.mtproto.{CRC32, Codec}
import org.slf4j.LoggerFactory
import scodec.bits.ByteVector

import scala.concurrent.Future

/**
  * Entry point.
  */
object Main extends App {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind("0.0.0.0", 8888)

  connections.runForeach { connection =>
    val handler = Flow[ByteString]
      .via(MTProtoFramer)
      .map(_.generateResponse)
      .map(_.toByteString)
    connection.handleWith(handler)
  }

  log.info("Use the ReqPQCodec and ReqDHParams samples packets for debugging:")
  log.info(Codec.ReqPQCodec.encode(CRC32.ReqPQValue -> ByteVector.fromInt(16)).toString + " " +
    Codec.ReqDHParams.encode(CRC32.ReqDHParamsValue ->
      ByteVector.fromInt(16) ->
      ByteVector.fromInt(16) -> "Hello" -> "World" -> 256L -> "ecrupted").map(_.toByteVector).toString
  )

  // Debug with:
  // echo '6046977800000010000000000000000000000000d712e4be00000010000000000000000000000000000000100000000000000000000000000000000548656c6c6f00000005576f726c640000000000000100000000086563727570746564' \
  // | xxd -r -p | nc localhost 8888
}
