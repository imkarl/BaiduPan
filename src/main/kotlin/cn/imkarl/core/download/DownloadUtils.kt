package cn.imkarl.core.download

import cn.imkarl.core.utils.LogUtils
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

/**
 * 下载相关工具类
 * @author imkarl
 */
internal object DownloadUtils {

    /**
     * 生成Header值：Range
     * @return 如果生成失败，返回空字符串
     */
    fun buildRangeHeader(offsetBytes: Long): String {
        return if (offsetBytes > 0) "bytes=$offsetBytes-" else ""
    }


    /**
     * 从Headers中获取响应内容的Etag
     *
     * @return 如果生成etag失败，返回空字符串
     */
    fun getMD5(headers: Headers?): String? {
        if (headers == null || headers.size() == 0) {
            return null
        }

        var md5 = headers.get("Content-MD5")
        if (md5.isNullOrEmpty()) {
            md5 = headers.get("ETag")
        }
        if (!md5.isNullOrEmpty()) {
            if (md5!!.length > 2 && md5.startsWith("\"") && md5.endsWith("\"")) {
                md5 = md5.substring(1, md5.length - 1)
                if (md5.endsWith(".gz")) {
                    md5 = md5.substring(0, md5.length - 3)
                }
            }
        }

        return md5
    }

    /**
     * 判断是否为有效文件
     */
    fun isAvailableFile(cacheFile: File): Boolean {
        return cacheFile.exists() && cacheFile.length() > 0
    }

    /**
     * 判断该响应内容是否支持分片下载
     */
    fun isSupportRange(response: Response<*>): Boolean {
        val headers = response.headers()

        if (headers == null || headers.size() == 0) {
            return false
        }

        var ranges = headers.get("Accept-Ranges")
        if (ranges.isNullOrEmpty()) {
            ranges = headers.get("Content-Range")
        }

        return ranges != null && ranges.contains("bytes")
    }

    /**
     * 获取预期完整内容的总大小
     *
     * @return -1 表示获取失败
     */
    fun getTotalContentLength(response: Response<ResponseBody>): Long {
        if (isSupportRange(response)) {
            // 支持分片，从"Content-Range"获取总大小
            val headers = response.headers()
            if (headers == null || headers.size() == 0) {
                return -1
            }

            val range = headers.get("Content-Range")
            if (range != null && range.contains("bytes")) {
                try {
                    return range.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toLongOrNull() ?: -1
                } catch (e: Exception) {
                    LogUtils.e(e)
                }

            }
            return -1

        } else {
            // 不支持分片，直接读取"Content-Length"
            val responseBody = response.body()
            return if (responseBody != null) {
                responseBody.contentLength()
            } else {
                -1
            }
        }
    }

}