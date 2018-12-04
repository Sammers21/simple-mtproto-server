package com.github.sammers21.mtproto

import scodec._
import scodec.bits._
import scodec.codecs.{bytes, _}

object Codec {

  // req_pq#60469778 nonce:int128 = ResPQ
  val ReqPQCodec: Codec[(ByteVector, ByteVector)] =
    bytes(4) ~ bytes(16)

  // resPQ#05162463 nonce:int128 server_nonce:int128 pq:string server_public_key_fingerprints:Vector long = ResPQ
  val ResPQCodec: Codec[(((((ByteVector, ByteVector), ByteVector), String), ByteVector), List[Long])] =
    bytes(4) ~ bytes(16) ~ bytes(16) ~ ascii32 ~ bytes(4) ~ listOfN(int32, long(64))

  // req_DH_params#d712e4be nonce:int128 server_nonce:int128 p:string q:string public_key_fingerprint:long encrypted_data:string = Server_DH_Params
  val ReqDHParams: Codec[((((((ByteVector, ByteVector), ByteVector), String), String), Long), String)] =
    bytes(4) ~ bytes(16) ~ bytes(16) ~ ascii32 ~ ascii32 ~ long(64) ~ ascii32
}
