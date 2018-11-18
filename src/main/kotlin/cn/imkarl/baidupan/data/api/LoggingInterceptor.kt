package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.core.utils.LogLevel
import cn.imkarl.core.utils.LogUtils
import okhttp3.*
import okio.Buffer
import org.joor.Reflect
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日志相关的Interceptor
 * @author imkarl
 */
class LoggingInterceptor : Interceptor {

    companion object {

        private val HEADER_FORM_DATA = "form-data; name=\""
        private val HEADER_FILENAME = "\"; filename=\""
        /** 输出Headers  */
        private val OUTPUT_HEADERS = false
        /** 输出长文本  */
        private val OUTPUT_LONG_TEXT = false
        /** 不输出长文本时，最大输出长度  */
        private val OUTPUT_MAX_LEN = 1 * 1024

        @Throws(IOException::class)
        private fun readString(body: RequestBody?): String? {
            if (body == null) {
                return null
            }

            val contentType = body.contentType()
            var charset: Charset? = Charset.forName("UTF-8")
            if (contentType != null) {
                charset = contentType.charset(charset)
            }

            val buffer = Buffer()
            body.writeTo(buffer)
            return readString(buffer, charset)
        }

        private fun readString(buffer: Buffer, charset: Charset?): String {
            val kb = buffer.size() / 1024 + 1
            val allBytes = buffer.readByteArray()

            val result: String
            var content = if (charset!=null) String(allBytes, charset) else String(allBytes)
            try {
                content = URLDecoder.decode(content, charset?.name() ?: "UTF-8")
            } catch (ignored: Exception) {
            }

            // 不输出长文本时，超过指定长度则裁剪
            if (!OUTPUT_LONG_TEXT && content.length > OUTPUT_MAX_LEN) {
                result = content.substring(0, OUTPUT_MAX_LEN) + " [ " + kb + "kb ]"
            } else {
                result = content
            }
            return result
        }

        private val TEXT_TYPE = Arrays.asList("text", "json")

        /**
         * 是否为纯文本
         * @return true表示纯文本
         *
         * @see [HTTP Content-type](http://tool.oschina.net/commons)
         */
        private fun isPlainText(contentType: MediaType?): Boolean {
            return contentType != null && (TEXT_TYPE.contains(contentType.type()) || TEXT_TYPE.contains(contentType.subtype()))
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 过滤下载请求
        if (!AppConfig.showHttpLogger
                || request.url().toString().startsWith("https://pcs.baidu.com/rest/2.0/pcs/file?method=download")) {
            return chain.proceed(request)
        }
        val logStr = StringBuilder()
        try {
            // Log request line
            val connection = chain.connection()
            val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
            logStr.append(request.method())
            logStr.append(' ')
            logStr.append(request.url())
            logStr.append(' ')
            logStr.append(protocol)
            logStr.append('\n')
            // Log request header
            if (OUTPUT_HEADERS) {
                val headers = request.headers()
                if (headers != null) {
                    logStr.append("Request Headers: ").append(toLogString(headers))
                }
            }
            // Log request body
            val requestBody = request.body()
            if (requestBody != null) {
                logStr.append("Request Body: ").append(toLogString(requestBody))
            }
            // Process request
            val startNs = System.nanoTime()
            val response = chain.proceed(request)
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            // Log response http status
            val responseBody = response.body()
            logStr.append(response.code())
            logStr.append(' ')
            logStr.append(response.message())
            logStr.append(" (")
            logStr.append(tookMs)
            logStr.append("ms")
            logStr.append(')')
            logStr.append('\n')
            // Log response header
            if (OUTPUT_HEADERS) {
                val headers = response.headers()
                if (headers != null) {
                    logStr.append("Response Headers: ").append(toLogString(headers))
                }
            }
            // Log response body
            var charset: Charset? = Charset.forName("UTF-8")
            val contentType = responseBody!!.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(charset)
                } catch (e: UnsupportedCharsetException) {
                    logStr.append("Couldn't decode the response body; charset is likely malformed.").append('\n')
                    charset = null
                }

            }
            if (charset != null && responseBody.contentLength() != 0L) {
                if (isPlainText(contentType)) {
                    val source = responseBody.source()
                    // Buffer the entire body.
                    source.request(java.lang.Long.MAX_VALUE)
                    val buffer = source.buffer()
                    val result = buffer.clone()
                    val responseContent = readString(result, charset)
                    if (responseContent.isEmpty()) {
                        logStr.append("[空]")
                    } else {
                        logStr.append(responseContent)
                    }
                } else {
                    logStr.append("[").append(if (contentType != null) contentType.type() + "/" + contentType.subtype() else "Unknown ContentType").append("]")
                }
            }
            return response
        } catch (e: Throwable) {
            logStr.append(e)
            throw e
        } finally {
            LogUtils.println(LogLevel.INFO, LogUtils.globalTag + ".HTTP", logStr.toString())
        }
    }


    private fun toLogString(headers: Headers?): String {
        val buffer = StringBuilder()
        if (headers != null && headers.size() > 0) {
            headers.names().forEach { name ->
                buffer.append("\n\t")
                buffer.append(if (name.isEmpty()) "" else name)
                buffer.append(": ")
                buffer.append(headers.get(name))
            }
        }
        if (!buffer.isEmpty()) {
            buffer.append('\n')
        }
        return buffer.toString()
    }

    @Throws(IOException::class)
    private fun toLogString(requestBody: RequestBody): String {
        val buffer = StringBuffer()
        if (requestBody is MultipartBody) {
            for (part in requestBody.parts()) {
                val (first, second) = readKeyValue(part) ?: continue

                buffer.append("\n\t")
                buffer.append(if (first.isEmpty()) "" else first)
                buffer.append('=')
                buffer.append(second)
            }
        } else {
            val body = readString(requestBody)
            if (body?.isEmpty() == false) {
                if (!body.contains("&")) {
                    buffer.append("\n\t")
                    buffer.append(body)
                } else {
                    body.split("&").forEach {
                        buffer.append("\n\t")
                        buffer.append(it)
                    }
                }
            }
        }
        if (!buffer.isEmpty()) {
            buffer.append('\n')
        }
        return buffer.append('\n').toString()
    }

    @Throws(IOException::class)
    private fun readKeyValue(part: MultipartBody.Part): Pair<String, String?>? {
        var headers: Headers? = null
        try {
            headers = Reflect.on(part).get("headers")
        } catch (ignored: Throwable) {
        }
        if (headers != null) {
            var key: String? = headers.get("Content-Disposition")
            if (key != null && !key.isEmpty() && key.startsWith(HEADER_FORM_DATA)) {
                key = key.substring(HEADER_FORM_DATA.length, key.length - 1)
                val indexFilename = key.indexOf(HEADER_FILENAME)
                if (indexFilename >= 0) {
                    val name = key.substring(0, indexFilename)
                    val filename = key.substring(indexFilename + HEADER_FILENAME.length, key.length - 1)
                    key = "$name[$filename]"
                }

                var body: RequestBody? = null
                try {
                    body = Reflect.on(part).get("body")
                } catch (ignored: Throwable) {
                }
                return Pair(key, readString(body))
            }
        }
        return null
    }

}
