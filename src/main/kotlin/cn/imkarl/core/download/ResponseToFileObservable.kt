package cn.imkarl.core.download

import cn.imkarl.core.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.*

/**
 * 将响应内容写入到本地文件
 * @author imkarl
 */
internal class ResponseToFileObservable : ObservableOnSubscribe<DownloadProgress> {

    companion object {
        // 更新进度的间隔时间
        private val UPDATE_INTERVAL_TIME: Long = 100
        // 读取缓冲大小
        private val READ_BUFFER_SIZE = 2048

        fun create(response: Response<ResponseBody>,
                   cacheFile: File): Observable<DownloadProgress> {
            return Observable.create(ResponseToFileObservable(response, cacheFile))
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
        }
    }

    // 上次更新时间
    private var lastUpdateTime: Long = 0

    private val response: Response<ResponseBody>
    private val cacheFile: File

    private constructor(response: Response<ResponseBody>,
                        cacheFile: File) {
        this.response = response
        this.cacheFile = cacheFile
    }

    @Throws(Exception::class)
    override fun subscribe(emitter: ObservableEmitter<DownloadProgress>) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val progress = DownloadProgress(cacheFile)
        try {
            val code = response.code()
            if (code < 200 || code >= 300) {
                // 非正常响应
                throw HttpException(response)
            }
            if (code == 204 || code == 205) {
                // 无响应内容
                throw HttpException(response)
            }

            val responseBody = response.body() ?: throw IOException("无法获取响应内容")

            inputStream = responseBody.byteStream()

            if (!DownloadUtils.isAvailableFile(cacheFile) || !DownloadUtils.isSupportRange(response)) {
                FileUtils.deleteFile(cacheFile)
                FileUtils.createNewFile(cacheFile)
                outputStream = FileOutputStream(cacheFile, false)

                progress.totalBytes = responseBody.contentLength()
                progress.downloadBytes = 0
                //LogUtils.println(LogLevel.INFO, "download... [full download]  totalBytes: ${progress.totalBytes}   last_downloadBytes: ${progress.downloadBytes}")
            } else {
                outputStream = FileOutputStream(cacheFile, true)

                progress.totalBytes = DownloadUtils.getTotalContentLength(response)
                progress.downloadBytes = FileUtils.size(cacheFile)
                //LogUtils.println(LogLevel.INFO, "download... [range download]  totalBytes: ${progress.totalBytes}   last_downloadBytes: ${progress.downloadBytes}")
            }

            emitter.onNext(progress)

            if (progress.totalBytes <= 0 || progress.totalBytes != progress.downloadBytes) {
                val buffer = ByteArray(READ_BUFFER_SIZE)
                var bytesRead: Int
                bytesRead = inputStream!!.read(buffer)
                while (bytesRead != -1 && !emitter.isDisposed) {
                    outputStream.write(buffer, 0, bytesRead)
                    progress.downloadBytes += bytesRead.toLong()

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime > UPDATE_INTERVAL_TIME) {
                        emitter.onNext(progress)

                        lastUpdateTime = currentTime
                        //val percent = calculatePercent(progress.downloadBytes, progress.totalBytes)
                        //LogUtils.println(LogLevel.INFO, "download... [" + DecimalFormat("0.000").format(percent) + "]   totalBytes: " + progress.totalBytes + "   downloadBytes: " + progress.downloadBytes)
                    }

                    if (!emitter.isDisposed) {
                        bytesRead = inputStream.read(buffer)
                    }
                }
            }
            //LogUtils.println(LogLevel.DEBUG, "download...  totalBytes: ${progress.totalBytes}   downloadBytes: ${progress.downloadBytes}")

            progress.isSuccess = true

            if (!emitter.isDisposed) {
                emitter.onNext(progress)
                emitter.onComplete()
            }
        } catch (e: Exception) {
            //LogUtils.println(LogLevel.WARN, "download...  totalBytes: ${progress.totalBytes}   downloadBytes: ${progress.downloadBytes}   readBytes/contentLength: " + (progress.downloadBytes - lastDownloadBytes) + if (response.body() != null) "/" + response.body()?.contentLength() else "-")
            if (!emitter.isDisposed) {
                emitter.onError(e)
            }
        } finally {
            outputStream?.let {
                try {
                    outputStream.flush()
                } catch (ignored: IOException) {
                }
            }
            inputStream?.close()
            outputStream?.close()
        }
    }

}
