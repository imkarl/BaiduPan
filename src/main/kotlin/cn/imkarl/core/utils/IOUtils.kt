package cn.imkarl.core.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * IO相关工具类
 * @author imkarl
 */
object IOUtils {

    /**
     * 复制数据
     */
    fun copy(source: InputStream, target: OutputStream): Long {
        try {
            var total: Long = 0

            var length: Int
            val buffer = ByteArray(2048)
            length = source.read(buffer)
            while (length >= 0) {
                target.write(buffer, 0, length)
                total += length.toLong()
                length = source.read(buffer)
            }

            return total
        } catch (e: IOException) {
            LogUtils.w(e)
        }

        return -1
    }

}