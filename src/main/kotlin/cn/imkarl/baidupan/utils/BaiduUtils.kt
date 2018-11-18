package cn.imkarl.baidupan.utils

/**
 * 百度相关工具类
 * @author imkarl
 */
object BaiduUtils {

    fun parseDirHierarchy(dir: String): List<String> {
        var formatDir = dir.trim()
        if (formatDir.isEmpty()) {
            formatDir = "/"
        } else {
            formatDir = formatDir.removeSuffix("/")
        }

        val hierarchy = mutableListOf<String>()
        var startIndex: Int
        var findIndex = 0
        while (findIndex >= 0) {
            hierarchy.add(formatDir.subSequence(0, findIndex).toString())
            startIndex = findIndex + 1
            if (startIndex >= formatDir.length) {
                findIndex = -2
            } else {
                findIndex = formatDir.indexOf("/", startIndex)
            }
        }
        if (findIndex == -1) {
            hierarchy.add(formatDir)
        }
        return hierarchy
    }

}