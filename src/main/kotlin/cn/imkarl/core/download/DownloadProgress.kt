package cn.imkarl.core.download

import java.io.File

/**
 * 下载进度
 * @author imkarl
 */
data class DownloadProgress(
        val output: File,
        var downloadBytes: Long = -1,
        var totalBytes: Long = -1,
        var isSuccess: Boolean = false,
        var responseMD5: String = ""
)