package cn.imkarl.baidupan.utils

import cn.imkarl.core.utils.EncodeUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * @author imkarl
 */
object RsaUtil {

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    /**
     * 生成公钥
     * @param modulus
     * @param publicExponent
     * @return RSAPublicKey *
     * @throws Exception
     */
    fun generateRSAPublicKey(modulus: ByteArray, publicExponent: BigInteger): RSAPublicKey {
        val keyFac: KeyFactory?
        try {
            keyFac = KeyFactory.getInstance("RSA", "BC")
        } catch (ex: NoSuchAlgorithmException) {
            throw Exception(ex.message)
        }

        val pubKeySpec = RSAPublicKeySpec(BigInteger(modulus), publicExponent)
        try {
            return keyFac!!.generatePublic(pubKeySpec) as RSAPublicKey
        } catch (ex: InvalidKeySpecException) {
            throw Exception(ex.message)
        }

    }

    /**
     * 生成私钥
     * @param modulus
     * @param privateExponent
     * @return RSAPrivateKey *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun generateRSAPrivateKey(modulus: ByteArray, privateExponent: ByteArray): RSAPrivateKey {
        var keyFac: KeyFactory? = null
        try {
            keyFac = KeyFactory.getInstance("RSA", "BC")
        } catch (ex: NoSuchAlgorithmException) {
            throw Exception(ex.message)
        }

        val priKeySpec = RSAPrivateKeySpec(BigInteger(modulus), BigInteger(privateExponent))
        try {
            return keyFac!!.generatePrivate(priKeySpec) as RSAPrivateKey
        } catch (ex: InvalidKeySpecException) {
            throw Exception(ex.message)
        }

    }

    /**
     * * 加密 *
     *
     * @param key
     * 加密的密钥 *
     * @param data
     * 待加密的明文数据 *
     * @return 加密后的数据 *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encrypt(pk: PublicKey, data: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("RSA", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, pk)
            val blockSize = cipher.blockSize// 获得加密块大小，如：加密前数据为128个byte，而key_size=1024
            // 加密块大小为127
            // byte,加密后为128个byte;因此共有2个加密块，第一个127
            // byte第二个为1个byte
            val outputSize = cipher.getOutputSize(data.size)// 获得加密块加密后块大小
            val leavedSize = data.size % blockSize
            val blocksSize = if (leavedSize != 0) data.size / blockSize + 1 else data.size / blockSize
            val raw = ByteArray(outputSize * blocksSize)
            var i = 0
            while (data.size - i * blockSize > 0) {
                if (data.size - i * blockSize > blockSize)
                    cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize)
                else
                    cipher.doFinal(data, i * blockSize, data.size - i * blockSize, raw, i * outputSize)
                // 这里面doUpdate方法不可用，查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到
                // ByteArrayOutputStream中，而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了
                // OutputSize所以只好用dofinal方法。

                i++
            }
            return raw
        } catch (e: Exception) {
            throw Exception(e.message)
        }

    }

    /**
     * 公钥加密
     */
    fun encrypt(data: String, publicKey: String): String {
        val pubKey = generateRSAPublicKey(BigInteger(publicKey, 16).toByteArray(),
                BigInteger.valueOf(0x10001))
        return EncodeUtils.bytesToHexString(encrypt(pubKey, data.toByteArray()))!!
    }

    /**
     * * *
     *
     * @param args
     * *
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // RSAPublicKey rsap = (RSAPublicKey)
        // RsaUtil.generateKeyPair().getPublic();
        // String test = "hello world";
        // byte[] en_test = encrypt(getKeyPair().getPublic(), test.getBytes());
        // byte[] de_test = decrypt(getKeyPair().getPrivate(), en_test);
        // System.out.println(new String(de_test));
        val text = "123456"
        val pubkey = "8a793e68528f7803547f9879701e431c99bd25e649c32506df49a76cb284676832258f9501c774139a109361f132321065d4ccb54c7b14efc45179a563946d45d15f21785211e0251f25f52aaa75de385494674baa45cde4ae817217693cc988588254d4ee37c46a4cae36da3164db5bc37993bd9ebdff9efe2b27e879153f09"
        var endata = encrypt(text, pubkey)
        println(endata)
        endata = "835e3bd901655b97b94ccdfcf03592ae525a45b1e9267f9088bfa719c4e26f456d8acd5517931859b78c66bfe85a89ddf215f0bc750cad28b6bb17b0b2845c99483ebf74e94ed3d0b0f151adae7f0b66c2a2a5ba1fe787d6d10839c8c6453f10602528a4e350b33674886fe7679d148ca999634ec4e35d61e0c57a8bf71e0d9d"
        val prikey = "8a793e68528f7803547f9879701e431c99bd25e649c32506df49a76cb284676832258f9501c774139a109361f132321065d4ccb54c7b14efc45179a563946d45d15f21785211e0251f25f52aaa75de385494674baa45cde4ae817217693cc988588254d4ee37c46a4cae36da3164db5bc37993bd9ebdff9efe2b27e879153f09"
        val privateKey = generateRSAPrivateKey(BigInteger(prikey, 16).toByteArray(),
                BigInteger(
                        "71e00941737bc69f3e72fbea0a18e8e9f1484a8d9a655fe2c9e76147137bad2a53eaedac055d8808c2af14f4fb8c62fd7730cbf3e0646bb04dcb0ef5c2f181f62de9b296c11f215f4f7d0c727416f81eb3b51e821162067d19b0565142ae3b9aa8d277f1a8a4f13599980909c39e2f98fe0ff5701363004af3ee4aab5c88720d",
                        16).toByteArray())
//        val de_test = decrypt(privateKey, CodeUtil.hexString2ByteArr(endata))
//        println(String(de_test))
    }

}