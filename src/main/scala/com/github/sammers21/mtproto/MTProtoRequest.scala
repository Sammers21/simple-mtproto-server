package com.github.sammers21.mtproto

import akka.util.ByteString
import scodec.bits.ByteVector
import scodec.interop.akka._

sealed trait MTProtoRequest {
  def generateResponse: MTProtoResponse
}

sealed trait MTProtoResponse {
  def toByteString: ByteString
}

// req_pq#60469778 nonce:int128 = ResPQ
case class ReqPQ(nonce: ByteVector) extends MTProtoRequest {

  // a random response
  override def generateResponse: MTProtoResponse = {
    ResPQ(
      ByteVector.low(16),
      ByteVector.low(16),
      "hello",
      List(1L, 2L, 3L)
    )
  }
}

// resPQ#05162463 nonce:int128 server_nonce:int128 pq:string server_public_key_fingerprints:Vector long = ResPQ
case class ResPQ(nonce: ByteVector,
            server_nonce: ByteVector,
            pq: String,
            server_public_key_fingerprints: List[Long]) extends MTProtoResponse {
  override def toByteString: ByteString = {
    Codec.ResPQCodec.encode(CRC32.ResPQValue -> nonce -> server_nonce -> pq -> CRC32.VectorValue -> server_public_key_fingerprints).getOrElse(
      throw new IllegalStateException("Unable to encode ReqRQ packet")
    ).toByteVector.toByteString
  }
}

// req_DH_params#d712e4be nonce:int128 server_nonce:int128 p:string q:string public_key_fingerprint:long encrypted_data:string = Server_DH_Params
case class ReqDHParams(nonce: ByteVector,
                  server_nonce: ByteVector,
                  p: String,
                  q: String,
                  public_key_fingerprint: Long,
                  encrypted_data: String) extends MTProtoRequest {
  override def generateResponse: MTProtoResponse = {
    new MTProtoResponse {
      override def toByteString: ByteString = ByteString.empty
    }
  }
}