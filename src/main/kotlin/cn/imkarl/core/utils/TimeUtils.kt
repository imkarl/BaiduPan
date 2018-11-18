package cn.imkarl.core.utils

/**
 * @author imkarl
 */
object TimeUtils {

    private val TIME_S = 1000L
    private val TIME_MIN = 60 * TIME_S
    private val TIME_HOURS = 60 * TIME_MIN
    private val TIME_DAY = 24 * TIME_HOURS

    fun formatDuration(duration: Long): String {
        if (duration > TIME_DAY) {
            val day = duration / TIME_DAY
            val hours = (duration % TIME_DAY) / TIME_HOURS
            val min = (duration % TIME_HOURS) / TIME_MIN
            val s = (duration % TIME_MIN) / TIME_S
            return "${day}天 ${hours}小时 ${min}分 ${s}秒"
        }
        if (duration > TIME_HOURS) {
            val hours = (duration % TIME_DAY) / TIME_HOURS
            val min = (duration % TIME_HOURS) / TIME_MIN
            val s = (duration % TIME_MIN) / TIME_S
            return "${hours}小时 ${min}分 ${s}秒"
        }
        if (duration > TIME_MIN) {
            val min = (duration % TIME_HOURS) / TIME_MIN
            val s = (duration % TIME_MIN) / TIME_S
            return "${min}分 ${s}秒"
        }
        if (duration > TIME_S) {
            val s = (duration % TIME_MIN) / TIME_S
            val ms = duration % TIME_S
            return "${s}秒 ${ms}ms"
        }
        return "0秒 ${duration}ms"
    }

}
