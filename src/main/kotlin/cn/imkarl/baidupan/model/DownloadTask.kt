package cn.imkarl.baidupan.model

/**
 * 下载任务
 * @author imkarl
 */
data class DownloadTask(
        val fileInfo: BaiduFile,
        var isDir: Boolean,
        var isSuccess: Boolean = false
)