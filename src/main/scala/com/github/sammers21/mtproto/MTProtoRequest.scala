package com.github.sammers21.mtproto

import akka.util.ByteString

sealed trait MTProtoRequest {
  def generateResponse: MTProtoResponse
}

sealed trait MTProtoResponse {
  def toByteString: ByteString
}

// req_pq#60469778 nonce:int128 = ResPQ
class ReqPQ(nonce: ByteString) extends MTProtoRequest {
  override def generateResponse: MTProtoResponse = {
    ???
  }
}

// nonce:int128 server_nonce:int128 pq:string server_public_key_fingerprints:Vector long
class ResPQ(nonce: ByteString,
            server_nonce: ByteString,
            pq: ByteString,
            server_public_key_fingerprints: Vector[Long]) extends MTProtoResponse {
  override def toByteString: ByteString = {
    ???
  }
}

// req_DH_params#d712e4be nonce:int128 server_nonce:int128 p:string q:string public_key_fingerprint:long encrypted_data:string = Server_DH_Params
class ReqDHParams(nonce: ByteString,
                  server_nonce: ByteString,
                  p: ByteString,
                  q: ByteString,
                  public_key_fingerprint: Long,
                  encrypted_data: ByteString) extends MTProtoRequest {
  override def generateResponse: MTProtoResponse = {
    ???
  }
}