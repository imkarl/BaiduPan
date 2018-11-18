package cn.imkarl.core.utils

import io.reactivex.annotations.NonNull
import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 加解密相关工具类
 * @author imkarl
 */
object EncryptUtils {

    val ALGORITHM_MD2 = "MD2"
    val ALGORITHM_MD5 = "MD5"
    val ALGORITHM_SHA_1 = "SHA-1"
    val ALGORITHM_SHA_256 = "SHA-256"
    val ALGORITHM_SHA_384 = "SHA-384"
    val ALGORITHM_SHA_512 = "SHA-512"
    val ALGORITHM_HMAC_MD5 = "HmacMD5"
    val ALGORITHM_HMAC_SHA1 = "HmacSHA1"
    val ALGORITHM_HMAC_SHA256 = "HmacSHA256"
    val ALGORITHM_HMAC_SHA512 = "HmacSHA512"


    /**
     * MD5加密
     *
     * @param data 待加密的字符串
     * @return 32位MD5校验码
     */
    fun md5(@NonNull data: String): String? {
        return md5(data.toByteArray())
    }

    /**
     * MD5加密
     *
     * @param data 待加密的字节数组
     * @return 32位MD5校验码
     */
    fun md5(@NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hashTemplate(ALGORITHM_MD5, data))
    }

    /**
     * MD5加密
     *
     * @param file 待加密的文件
     * @return 32位MD5校验码
     */
    fun md5(@NonNull file: File): String? {
        var fis: FileInputStream? = null
        val digestInputStream: DigestInputStream
        try {
            fis = FileInputStream(file)
            var md = MessageDigest.getInstance("MD5")
            digestInputStream = DigestInputStream(fis, md)
            val buffer = ByteArray(256 * 1024)
            while (true) {
                if (digestInputStream.read(buffer) <= 0) {
                    break
                }
            }
            md = digestInputStream.messageDigest
            return EncodeUtils.bytesToHexString(md.digest())
        } catch (e: Exception) {
            System.err.println("${e::class.simpleName}: ${e.message}")
            return null
        } finally {
            fis.closeQuietly()
        }
    }

    /**
     * SHA1加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha1(@NonNull data: String): String? {
        return sha1(data.toByteArray())
    }

    /**
     * SHA1加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha1(@NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hashTemplate(ALGORITHM_SHA_1, data))
    }

    /**
     * SHA256加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha256(@NonNull data: String): String? {
        return sha256(data.toByteArray())
    }

    /**
     * SHA256加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha256(@NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hashTemplate(ALGORITHM_SHA_256, data))
    }

    /**
     * SHA512加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha512(@NonNull data: String): String? {
        return sha512(data.toByteArray())
    }

    /**
     * SHA512加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha512(@NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hashTemplate(ALGORITHM_SHA_512, data))
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5ToString(@NonNull key: String, @NonNull data: String): String? {
        return hmacMD5ToString(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5ToString(@NonNull key: ByteArray, @NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hmacMD5(key, data))
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5(@NonNull key: String, @NonNull data: String): ByteArray? {
        return hmacMD5(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5(@NonNull key: ByteArray, @NonNull data: ByteArray): ByteArray? {
        return hmacTemplate(ALGORITHM_HMAC_MD5, key, data)
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1ToString(@NonNull key: String, @NonNull data: String): String? {
        return hmacSHA1ToString(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1ToString(@NonNull key: ByteArray, @NonNull data: ByteArray): String? {
        return EncodeUtils.bytesToHexString(hmacSHA1(key, data))
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1(@NonNull key: String, @NonNull data: String): ByteArray? {
        return hmacSHA1(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1(@NonNull key: ByteArray, @NonNull data: ByteArray): ByteArray? {
        return hmacTemplate(ALGORITHM_HMAC_SHA1, key, data)
    }

    /**
     * HmacSHA256加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA256(@NonNull key: String, @NonNull data: String): ByteArray? {
        return hmacSHA256(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA256加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA256(@NonNull key: ByteArray, @NonNull data: ByteArray): ByteArray? {
        return hmacTemplate(ALGORITHM_HMAC_SHA256, key, data)
    }

    /**
     * HmacSHA512加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA512(@NonNull key: String, @NonNull data: String): ByteArray? {
        return hmacSHA512(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA256加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA512(@NonNull key: ByteArray, @NonNull data: ByteArray): ByteArray? {
        return hmacTemplate(ALGORITHM_HMAC_SHA512, key, data)
    }

    /**
     * hash加密模板
     *
     * @param algorithm 加密算法
     * @param data      待加密的数据
     * @return 密文字节数组
     */
    private fun hashTemplate(@NonNull algorithm: String, @NonNull data: ByteArray): ByteArray? {
        try {
            if (data.isNotEmpty()) {
                val md = MessageDigest.getInstance(algorithm)
                md.update(data)
                return md.digest()
            }
        } catch (e: NoSuchAlgorithmException) {
            LogUtils.e(e)
        }

        return null
    }

    /**
     * Hmac加密模板
     *
     * @param algorithm 加密算法
     * @param key       秘钥
     * @param data      待加密的数据
     * @return 密文字节数组
     */
    private fun hmacTemplate(@NonNull algorithm: String, @NonNull key: ByteArray, @NonNull data: ByteArray): ByteArray? {
        try {
            if (key.isNotEmpty() && data.isNotEmpty()) {
                val mac = Mac.getInstance(algorithm)
                val secretKey = SecretKeySpec(key, algorithm)
                mac.init(secretKey)
                return mac.doFinal(data)
            }
        } catch (e: InvalidKeyException) {
            LogUtils.e(e)
        } catch (e: NoSuchAlgorithmException) {
            LogUtils.e(e)
        }

        return null
    }


}