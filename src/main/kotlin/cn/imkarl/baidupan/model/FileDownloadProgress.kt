package cn.imkarl.baidupan.model

/**
 * 下载进度
 * @author imkarl
 */
data class FileDownloadProgress(
        val path: String,
        val filename: String,
        var isDir: Boolean,
        var isSuccess: Boolean,
        var totalSize: Long,
        var downloadSize: Long,
        var isStartup: Boolean
) {
    constructor(fileInfo: BaiduFile): this(
            fileInfo.path,
            fileInfo.filename,
            fileInfo.isDir,
            false,
            if (fileInfo.isDir) -1 else fileInfo.size,
            -1,
            false
    )

    fun getFileInfo(): BaiduFile {
        return BaiduFile(
                path=path,
                fs_id="",
                server_ctime=0,
                server_mtime=0,
                local_ctime=0,
                local_mtime=0,
                filename=filename,
                size=totalSize,
                md5="",
                isDir=isDir)
    }
}