package scorex

import java.net.{InetAddress, InetSocketAddress}

import org.scalacheck.{Arbitrary, Gen}
import scorex.core.app.Version
import scorex.core.network.message.BasicMsgDataTypes._
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.transaction.state.{PrivateKey25519, PrivateKey25519Companion}
import scorex.core.{ModifierId, ModifierTypeId, NodeViewModifier}
import scorex.crypto.signatures.Curve25519

trait ObjectGenerators {

  val MaxVersion = 999
  val MaxIp = 255
  val MaxPort = 65535

  lazy val smallInt: Gen[Int] = Gen.choose(0, 20)

  lazy val nonEmptyBytesGen: Gen[Array[Byte]] = Gen.nonEmptyListOf(Arbitrary.arbitrary[Byte])
    .map(_.toArray).suchThat(_.length > 0)

  def genBoundedBytes(minSize: Int, maxSize: Int): Gen[Array[Byte]] = {
    Gen.choose(minSize, maxSize) flatMap { sz => Gen.listOfN(sz, Arbitrary.arbitrary[Byte]).map(_.toArray) }
  }

  def genBytesList(size: Int): Gen[Array[Byte]] = genBoundedBytes(size, size)

  lazy val positiveLongGen: Gen[Long] = Gen.choose(1, Long.MaxValue)

  lazy val positiveByteGen: Gen[Byte] = Gen.choose(1, Byte.MaxValue)


  lazy val modifierIdGen: Gen[ModifierId] = Gen.listOfN(NodeViewModifier.ModifierIdSize, Arbitrary.arbitrary[Byte])
    .map(id => ModifierId @@ id.toArray)

  lazy val modifierTypeIdGen: Gen[ModifierTypeId] = Arbitrary.arbitrary[Byte].map(t => ModifierTypeId @@ t)

  lazy val invDataGen: Gen[InvData] = for {
    modifierTypeId: ModifierTypeId <- modifierTypeIdGen
    modifierIds: Seq[ModifierId] <- Gen.nonEmptyListOf(modifierIdGen) if modifierIds.nonEmpty
  } yield modifierTypeId -> modifierIds

  lazy val modifierWithIdGen: Gen[(ModifierId, Array[Byte])] = for {
    id <- modifierIdGen
    mod <- nonEmptyBytesGen
  } yield id -> mod

  lazy val modifiersGen: Gen[ModifiersData] = for {
    modifierTypeId: ModifierTypeId <- modifierTypeIdGen
    modifiers: Map[ModifierId, Array[Byte]] <- Gen.nonEmptyMap(modifierWithIdGen).suchThat(_.nonEmpty)
  } yield modifierTypeId -> modifiers



  lazy val appVersionGen = for {
    fd <- Gen.choose(0: Byte, Byte.MaxValue)
    sd <- Gen.choose(0: Byte, Byte.MaxValue)
    td <- Gen.choose(0: Byte, Byte.MaxValue)
  } yield Version(fd, sd, td)

  lazy val inetSocketAddressGen = for {
    ip1 <- Gen.choose(0, MaxIp)
    ip2 <- Gen.choose(0, MaxIp)
    ip3 <- Gen.choose(0, MaxIp)
    ip4 <- Gen.choose(0, MaxIp)
    port <- Gen.choose(0, MaxPort)
  } yield new InetSocketAddress(InetAddress.getByName(s"$ip1.$ip2.$ip3.$ip4"), port)

  lazy val key25519Gen: Gen[(PrivateKey25519, PublicKey25519Proposition)] = genBytesList(Curve25519.KeyLength)
    .map(s => PrivateKey25519Companion.generateKeys(s))

  lazy val propositionGen: Gen[PublicKey25519Proposition] = key25519Gen.map(_._2)
}
