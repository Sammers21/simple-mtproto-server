package com.github.sammers21.mtproto

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import org.slf4j.LoggerFactory
import scodec.Attempt.{Failure, Successful}
import scodec._
import scodec.bits._
import scodec.interop.akka._


object Framing {

  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * @return bytes flow -> MTProtoRequest flow
    */
  def MTProtoFramer: Flow[ByteString, MTProtoRequest, NotUsed] =
    Flow.fromGraph[ByteString, MTProtoRequest, NotUsed](new MTProtoFramingStage())


  class MTProtoFramingStage() extends GraphStage[FlowShape[ByteString, MTProtoRequest]] {

    val in = Inlet[ByteString]("MTProtoFramingStage.in")
    val out = Outlet[MTProtoRequest]("MTProtoFramingStage.out")
    override val shape: FlowShape[ByteString, MTProtoRequest] = FlowShape(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with InHandler with OutHandler {
      private var buffer = ByteVector.empty

      private def parse(): Unit = {
        buffer.slice(0, 4) match {
          case CRC32.ReqPQValue =>
            Codec.ReqPQCodec.decode(buffer.bits) match {
              case Successful(decodeResult: DecodeResult[(ByteVector, ByteVector)]) =>
                buffer = decodeResult.remainder.toByteVector
                val itemToEmit = ReqPQ(decodeResult.value._2)
                log.info("received: " + itemToEmit)
                push(out, itemToEmit)
              case Failure(cause) =>
                println(cause.messageWithContext)
            }
          case CRC32.ReqDHParamsValue =>
            Codec.ReqDHParams.decode(buffer.bits) match {
              case Successful(decodeResult: DecodeResult[((((((ByteVector, ByteVector), ByteVector), String), String), Long), String)]) =>
                buffer = decodeResult.remainder.toByteVector
                val encrypted_data = decodeResult.value._2
                val public_key_fingerprint = decodeResult.value._1._2
                val q = decodeResult.value._1._1._2
                val p = decodeResult.value._1._1._1._2
                val server_nonce = decodeResult.value._1._1._1._1._2
                val nonce = decodeResult.value._1._1._1._1._1._2
                val itemToEmit = ReqDHParams(nonce, server_nonce, p, q, public_key_fingerprint, encrypted_data)
                log.info("received: " + itemToEmit)
                push(out, itemToEmit)
                log.info("closing the connection")
                completeStage()
              case Failure(cause) =>
                tryPoll()
                println(cause.messageWithContext)
            }
          case buf if buf.size < 4 =>
            tryPoll()
          case different =>
            failStage(new Exception("Unknown message with header: " + different))
        }
      }

      private def tryPoll(): Unit = {
        if (isClosed(in)) {
          completeStage()
        } else pull(in)
      }

      override def onPush(): Unit = {
        buffer ++= grab(in).toByteVector
        parse()
      }

      override def onPull(): Unit = {
        parse()
      }

      setHandlers(in, out, this)
    }
  }

}
