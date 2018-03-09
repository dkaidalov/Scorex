package examples.commons

import com.google.common.primitives.Longs
import io.circe.Json
import io.circe.syntax._
import scorex.core.serialization.{JsonSerializable, Serializer}
import scorex.core.transaction.account.PublicKeyNoncedBox
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.crypto.encode.{Base16, Base58}
import scorex.crypto.hash.Blake2b256
import scorex.crypto.signatures.{Curve25519, PublicKey}

import scala.util.Try

case class PublicKey25519NoncedBox(override val proposition: PublicKey25519Proposition,
                                   override val nonce: Nonce,
                                   override val value: Value) extends PublicKeyNoncedBox[PublicKey25519Proposition] with JsonSerializable {

  override def json: Json = Map(
    "id" -> Base58.encode(id).asJson,
    "address" -> proposition.address.asJson,
    "publicKey" -> Base58.encode(proposition.pubKeyBytes).asJson,
    "nonce" -> nonce.toLong.asJson,
    "value" -> value.toLong.asJson
  ).asJson

  override type M = PublicKey25519NoncedBox

  override def serializer: Serializer[PublicKey25519NoncedBox] = PublicKey25519NoncedBoxSerializer

  override def toString: String =
    s"PublicKey25519NoncedBox(id: ${Base16.encode(id)}, proposition: $proposition, nonce: $nonce, value: $value)"
}

object PublicKey25519NoncedBox {
  val BoxKeyLength = Blake2b256.DigestSize
  val BoxLength: Int = Curve25519.KeyLength + 2 * 8
}

object PublicKey25519NoncedBoxSerializer extends Serializer[PublicKey25519NoncedBox] {

  override def toBytes(obj: PublicKey25519NoncedBox): Array[Byte] =
    obj.proposition.pubKeyBytes ++
      Longs.toByteArray(obj.nonce) ++
      Longs.toByteArray(obj.value)


  override def parseBytes(bytes: Array[Byte]): Try[PublicKey25519NoncedBox] = Try {
    val pk = PublicKey25519Proposition(PublicKey @@ bytes.take(32))
    val nonce = Nonce @@ Longs.fromByteArray(bytes.slice(32, 40))
    val value = Value @@ Longs.fromByteArray(bytes.slice(40, 48))
    PublicKey25519NoncedBox(pk, nonce, value)
  }
}

