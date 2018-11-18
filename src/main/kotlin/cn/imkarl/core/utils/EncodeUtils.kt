package cn.imkarl.core.utils

import io.reactivex.annotations.NonNull
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/**
 * 编解码相关工具类
 * Created by imkarl on 2017/09/7
 */
object EncodeUtils {

    val ISO_8859_1 = Charset.forName("ISO-8859-1")
    val UTF_8 = Charset.forName("UTF-8")
    val UTF_16BE = Charset.forName("UTF-16BE")
    val UTF_16LE = Charset.forName("UTF-16LE")
    val UTF_16 = Charset.forName("UTF-16")

    private val PATTERN_UNICODE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")

    /**
     * Base64编码
     *
     * @param data 待编码的字符
     * @return 编码结果
     */
    fun encodeBase64(@NonNull data: ByteArray): String? {
        try {
            return Base64.getEncoder().encodeToString(data)
        } catch (e: Exception) {
            LogUtils.e(e)
            return null
        }

    }

    /**
     * Base64解码
     *
     * @param data 待解码的字符
     * @return 解码结果
     */
    fun decodeBase64(@NonNull data: String): ByteArray? {
        try {
            return Base64.getDecoder().decode(data)
        } catch (e: Exception) {
            LogUtils.e(e)
            return null
        }

    }

    /**
     * URL编码
     *
     * @param data 待编码的字符
     * @param charset 字符集
     * @return 编码结果，若编码失败则直接将data原样返回
     */
    fun encodeUrl(data: String, charset: Charset = UTF_8): String {
        try {
            return URLEncoder.encode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            return data
        }
    }

    /**
     * URL解码
     *
     * @param data 待解码的字符
     * @param charset 字符集
     * @return 解码结果，若解码失败则直接将data原样返回
     */
    fun decodeUrl(data: String, charset: Charset = UTF_8): String {
        try {
            return URLDecoder.decode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            return data
        }
    }


    /**
     * Unicode编码
     *
     * @param data 待编码的字符
     * @return 编码结果，若编码失败则直接将data原样返回
     */
    fun encodeUnicode(data: String): String {
        val unicodeBytes = StringBuilder()
        for (ch in data.toCharArray()) {
            if (ch.toInt() < 10) {
                unicodeBytes.append("\\u000").append(Integer.toHexString(ch.toInt()))
                continue
            }

            if (Character.UnicodeBlock.of(ch) === Character.UnicodeBlock.BASIC_LATIN) {
                // 英文及数字等
                unicodeBytes.append(ch)
            } else {
                // to Unicode
                val hex = Integer.toHexString(ch.toInt())
                if (hex.length == 1) {
                    unicodeBytes.append("\\u000").append(hex)
                } else if (hex.length == 2) {
                    unicodeBytes.append("\\u00").append(hex)
                } else if (hex.length == 3) {
                    unicodeBytes.append("\\u0").append(hex)
                } else if (hex.length == 4) {
                    unicodeBytes.append("\\u").append(hex)
                }
            }
        }
        return unicodeBytes.toString()
    }

    /**
     * Unicode解码
     *
     * @param data 待解码的字符
     * @return 解码结果，若解码失败则直接将data原样返回
     */
    fun decodeUnicode(data: String): String {
        if (!data.contains("\\u")) {
            return data
        }

        val buf = StringBuffer()
        val matcher = PATTERN_UNICODE.matcher(data)
        while (matcher.find()) {
            try {
                matcher.appendReplacement(buf, "")
                buf.appendCodePoint(Integer.parseInt(matcher.group(1), 16))
            } catch (ignored: NumberFormatException) {
            }
        }
        matcher.appendTail(buf)
        return buf.toString()
    }


    /**
     * 字节数组转16进制字符串
     *
     * @param data 待转换的字节数组
     * @return 16进制字符串
     */
    fun bytesToHexString(data: ByteArray?): String? {
        if (data == null || data.size <= 0) {
            return null
        }

        val hexBuilder = StringBuilder()
        for (b in data) {
            val hv = Integer.toHexString(b.toInt() and 0xFF)
            if (hv.length < 2) {
                hexBuilder.append(0)
            }
            hexBuilder.append(hv)
        }
        return hexBuilder.toString()
    }

}
