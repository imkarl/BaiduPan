package cn.imkarl.core.utils

import cn.imkarl.baidupan.AppConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

/**
 * 日志相关工具类
 * @author imkarl
 */
object LogUtils {

    private val MAX_LINE_LENGTH = 3000 // 最大打印长度

    /**
     * 设置全局TAG
     */
    @JvmField
    var globalTag = AppConfig.appName

    @JvmStatic
    fun v(message: Any?) {
        log(LogLevel.VERBOSE, globalTag, message)
    }

    @JvmStatic
    fun d(message: Any?) {
        log(LogLevel.DEBUG, globalTag, message)
    }

    @JvmStatic
    fun i(message: Any?) {
        log(LogLevel.INFO, globalTag, message)
    }

    @JvmStatic
    fun w(message: Any?) {
        log(LogLevel.WARN, globalTag, message)
    }

    @JvmStatic
    fun e(message: Any?) {
        log(LogLevel.ERROR, globalTag, message)
    }

    @JvmStatic
    fun v(tag: String, message: Any?) {
        log(LogLevel.VERBOSE, tag, message)
    }

    @JvmStatic
    fun d(tag: String, message: Any?) {
        log(LogLevel.DEBUG, tag, message)
    }

    @JvmStatic
    fun i(tag: String, message: Any?) {
        log(LogLevel.INFO, tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: Any?) {
        log(LogLevel.WARN, tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: Any?) {
        log(LogLevel.ERROR, tag, message)
    }


    @JvmStatic
    fun println(level: LogLevel, msg: String) {
        console(level, globalTag, toString(msg))
    }
    @JvmStatic
    fun println(level: LogLevel, tag: String, msg: String) {
        console(level, tag, toString(msg))
    }


    private fun log(level: LogLevel, tag: String, message: Any?) {
        val element = Throwable().stackTrace[2]
        var className = element.className
        className = className.substring(className.lastIndexOf(".") + 1)
        val codeLine = className + '.' + element.methodName + '(' + element.fileName + ':' + element.lineNumber + ')'
        console(level, tag, codeLine + "\n\t" + toString(message))
    }

    /**
     * 真实的打印方法
     */
    private fun console(level: LogLevel, tag: String, msg: String) {
        if (msg.length <= MAX_LINE_LENGTH) {
            console(level, "$tag: $msg")
            return
        }

        // 超出目标长度，自动换行打印
        var startIndex = 0
        var endIndex = MAX_LINE_LENGTH
        while (endIndex > startIndex) {
            console(level, "$tag: ${(if (startIndex == 0) "" else "\t↑↑↑↑\n") + msg.substring(startIndex, endIndex)}")

            startIndex = endIndex
            endIndex = Math.min(msg.length, startIndex + MAX_LINE_LENGTH)
        }
    }
    private fun console(level: LogLevel, message: String) {
        if (LogLevel.ERROR == level || LogLevel.WARN == level) {
            System.err.println("[${level.name}] $message")
        } else {
            println("[${level.name}] $message")
        }
    }


    private fun toString(message: Any?): String {
        if (message == null) {
            return "[NULL]"
        }
        if (message is Throwable) {
            var t = message as Throwable?
            while (t != null) {
                if (t is UnknownHostException) {
                    return t.toString()
                }
                t = t.cause
            }

            val sw = StringWriter()
            val pw = PrintWriter(sw, false)
            message.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
        if (message is String && !message.isEmpty()) {
            val str = message as String?
            val isJsonFormat = str!!.startsWith("{") && str.endsWith("}") || str.startsWith("[") && str.endsWith("]")
            if (isJsonFormat) {
                try {
                    return JsonUtils.toJson(message)
                } catch (ignored: Exception) {
                }

            } else {
                return EncodeUtils.decodeUnicode(str)
            }
        }
        return message.toString()
    }

}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}
