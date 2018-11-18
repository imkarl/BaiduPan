package cn.imkarl.core.utils

import java.awt.Frame
import java.io.Closeable
import java.net.SocketException
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (e: Exception) {
    }
}

fun Frame?.dismiss() {
    this?.isVisible = false
    this?.dispose()
}

val Throwable?.description: String
    get() {
        if (this is SocketException) {
            return "网络繁忙，请稍后再试~"
        }
        return "未知错误"
    }


fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
