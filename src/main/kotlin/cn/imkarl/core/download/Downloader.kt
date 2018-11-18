package cn.imkarl.core.download

import cn.imkarl.baidupan.data.local.SettingPrefs
import cn.imkarl.baidupan.data.local.db.DownloadFileDao
import cn.imkarl.baidupan.data.local.db.DownloadTaskDao
import cn.imkarl.baidupan.data.repository.BaiduRepository
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.model.DownloadTask
import cn.imkarl.baidupan.model.FileDownloadProgress
import cn.imkarl.core.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * 下载器
 * @author imkarl
 */
object Downloader {

    // 最大任务数
    private val MAX_TASK_COUNT = 100

    // 等待处理的任务
    private val waitingTasks = mutableListOf<BaiduFile>()
    // 当前进行中的任务
    private val runingTasks = hashSetOf<BaiduFile>()

    private val refreshTaskSubject = PublishSubject.create<Int>()
    private var refreshTaskDisposable: Disposable? = null

    private val printTaskProgressSubject = PublishSubject.create<Int>()
    private var printTaskProgressDisposable: Disposable? = null

    private val downloadDisposableMap = mutableMapOf<String, Disposable>()

    private var time: Long = 0

    init {
        printTaskProgressDisposable = printTaskProgressSubject
                .throttleFirst(10, TimeUnit.SECONDS)
                .subscribe { _ ->
                    synchronized(waitingTasks) {
                        val duration = System.currentTimeMillis() - time
                        val totalSize = runingTasks.sumByLong { it.size } + waitingTasks.sumByLong { it.size }
                        val downloadSize = runingTasks.sumByLong { File(SettingPrefs.downloadDir, it.path + ".downloading").length() } + waitingTasks.sumByLong { File(SettingPrefs.downloadDir, it.path + ".downloading").length() }
                        println("------ "
                                + "${TimeUtils.formatDuration(duration)}    total: ${FileUtils.formatFileSize(totalSize)}  download: ${FileUtils.formatFileSize(downloadSize)},"
                                + "    totalTask: ${waitingTasks.size + runingTasks.size}  runingTask: ${runingTasks.size}"
                                + " ----")
                    }
                    if (waitingTasks.isEmpty() && runingTasks.isEmpty()) {
                        println("----- no download task -----")
                    }
                }
        refreshTaskDisposable = refreshTaskSubject.throttleFirst(30, TimeUnit.MILLISECONDS)
                .subscribe({
                    // 重新整理下载任务
                    var appendTaskCount = MAX_TASK_COUNT - runingTasks.size
                    if (appendTaskCount > 0) {
                        synchronized(waitingTasks) {
                            val iterator = waitingTasks.iterator()
                            while (iterator.hasNext() && appendTaskCount > 0) {
                                val file = iterator.next()
                                runingTasks.add(file)
                                startDownload(file)
                                iterator.remove()
                                appendTaskCount--
                            }
                        }
                    }

                    printTaskProgressSubject.onNext(1)
                }, {
                    LogUtils.d(it)
                })
        Observable.interval(30, TimeUnit.SECONDS)
                .subscribe {
                    printTaskProgressSubject.onNext(2)
                }
    }

    /**
     * 添加下载任务
     */
    fun putDownloadTask(dirOrFiles: List<BaiduFile>): Observable<Boolean> {
        return putDownloadTask(*dirOrFiles.toTypedArray())
    }
    fun putDownloadTask(vararg dirOrFiles: BaiduFile): Observable<Boolean> {
        if (dirOrFiles.isEmpty()) {
            return Observable.just(true)
        }

        DownloadTaskDao.insert(dirOrFiles.map { FileDownloadProgress(it) })
        DownloadTaskDao.updateIsStartup(dirOrFiles.map { FileDownloadProgress(it) }, true)
        if (dirOrFiles.size > 10) {
            println("downloadFile:  [size:${dirOrFiles.size}]")
        } else {
            println("downloadFile:  [${dirOrFiles.map { it.filename }.joinToString(", ")}]")
        }

        // 拆解文件夹
        return listFiles(dirOrFiles)
                .doFinally { refreshTaskSubject.onNext(1) }
                .map { files ->
                    DownloadTaskDao.updateIsStartup(dirOrFiles.map { FileDownloadProgress(it) }, true)
                    println("putDownloadTask: ${files.size}    [${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())}]")
                    val tasks = files.map { DownloadTask(it, it.isDir) }
                    val insertTask = DownloadFileDao.insert(tasks)
                    if (insertTask > 0) {
                        synchronized(waitingTasks) {
                            files.forEach { file ->
                                if (!waitingTasks.contains(file)) {
                                    waitingTasks.add(file)
                                }
                            }
                        }
                    }
                    println("append task count: $insertTask,  task: ${tasks.size}    [${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())}]")
                    true
                }
    }

    fun updateIsStartup(task: FileDownloadProgress, startup: Boolean): Observable<Boolean> {
        return listFiles(arrayOf(task.getFileInfo()))
                .observeOn(Schedulers.io())
                .map { files ->
                    if (startup) {
                        synchronized(waitingTasks) {
                            waitingTasks.addAll(files)
                        }
                        refreshTaskSubject.onNext(1)
                    } else {
                        files.forEach { file ->
                            synchronized(waitingTasks) {
                                waitingTasks.remove(file)
                                runingTasks.remove(file)
                            }

                            synchronized(downloadDisposableMap) {
                                downloadDisposableMap.get(file.path)?.apply {
                                    if (!this.isDisposed) {
                                        this.dispose()
                                    }
                                }
                            }
                        }
                        println("stop task: ${files.size}")
                        println("waitingTasks: ${waitingTasks.size}")
                        println("runingTasks: ${runingTasks.size}")
                        println("downloadDisposableMap: $downloadDisposableMap")
                        refreshTaskSubject.onNext(1)
                    }

                    DownloadTaskDao.updateIsStartup(task, startup)
                    true
                }
    }

    /**
     * 所有下载计划
     */
    fun listDownloadProject(): Observable<List<FileDownloadProgress>> {
        return Observable.just(0)
                .observeOn(Schedulers.io())
                .flatMap {
                    Observable.fromIterable(DownloadTaskDao.listAll())
                }
                .flatMap { task ->
                    listFiles(arrayOf(task.getFileInfo()))
                            .map { files ->
                                task.totalSize = files.sumByLong { it.size }
                                if (!task.isSuccess) {
                                    if (task.isDir && files.isEmpty()) {
                                        task.isSuccess = true
                                    }
                                }

                                if (task.isSuccess) {
                                    task.downloadSize = task.totalSize
                                } else {
                                    task.downloadSize = files.sumByLong { file ->
                                        val saveFile = File(SettingPrefs.downloadDir, file.path)
                                        if (saveFile.exists()
                                                && saveFile.length() == file.size) {
                                            DownloadFileDao.updateStatus(DownloadTask(file, file.isDir), true)
                                            saveFile.length()
                                        } else {
                                            val downloadFile = File(SettingPrefs.downloadDir, file.path + ".downloading")
                                            downloadFile.length()
                                        }
                                    }
                                }
                                if (task.downloadSize == task.totalSize) {
                                    DownloadTaskDao.updateStatus(task, true)
                                }

                                task
                            }
                }
                .toList()
                .toObservable()
    }

    private fun listFiles(dirOrFiles: Array<out BaiduFile>): Observable<List<BaiduFile>> {
        return Observable.fromIterable(dirOrFiles.toList())
                .flatMap<BaiduFile> { dirOrFile ->
                    if (dirOrFile.isDir) {
                        BaiduRepository.listFiles(dirOrFile.path)
                                .flatMap { listFiles(it.toTypedArray()) }
                                .flatMap { Observable.fromIterable(it) }
                    } else {
                        Observable.just(dirOrFile)
                    }
                }
                .toList()
                .toObservable()
    }

    /**
     * 恢复之前的下载任务
     */
    fun resumeHistoryDownloadTask() {
        println("所有任务记录总数：${DownloadTaskDao.listAll()?.size}")
        DownloadTaskDao.listAllByNotSuccess()?.let { allTasks ->
            listFiles(allTasks.map { it.getFileInfo() }.toTypedArray())
                    .observeOn(Schedulers.io())
                    .subscribe { files ->
                        synchronized(waitingTasks) {
                            waitingTasks.addAll(files)
                        }

                        println("恢复下载任务: ${allTasks.size},  files: ${files.size}")

                        time = System.currentTimeMillis()
                        refreshTaskSubject.onNext(1)
                    }
        }
    }

    /**
     * 开启一个下载任务
     */
    private fun startDownload(file: BaiduFile) {
        Observable.just(file)
                .observeOn(Schedulers.computation())
                .subscribe { _ ->
                    val downloadingFile = File(SettingPrefs.downloadDir, file.path+".downloading")
                    val saveFile = File(SettingPrefs.downloadDir, file.path)

                    if (saveFile.exists()
                            && saveFile.length() == file.size) {
                        synchronized(waitingTasks) {
                            runingTasks.remove(file)
                        }
                        DownloadFileDao.updateStatus(DownloadTask(file, file.isDir), true)
                        refreshTaskSubject.onNext(1)
                        return@subscribe
                    }

                    val downloadDisposable = BaiduRepository.download(file, downloadingFile)
                            .filter { it.isSuccess }
                            .take(1)
                            .doFinally {
                                refreshTaskSubject.onNext(1)
                                synchronized(downloadDisposableMap) {
                                    downloadDisposableMap.remove(file.path)
                                }
                            }
                            .subscribe({
                                var md5 = EncryptUtils.md5(downloadingFile)

                                if (downloadingFile.exists() && (md5?.equals(file.md5) == true || md5?.equals(it.responseMD5) == true)) {
                                    FileUtils.rename(downloadingFile, saveFile)
                                }
                                if (saveFile.exists()) {
                                    md5 = EncryptUtils.md5(saveFile)
                                }
                                if (md5?.equals(file.md5) == true || md5?.equals(it.responseMD5) == true) {
                                    //println("下载成功: ${file.path}   fileLength:${FileUtils.formatFileSize(saveFile.length())}  info.size:${FileUtils.formatFileSize(file.size)}")
                                    synchronized(waitingTasks) {
                                        runingTasks.remove(file)
                                    }
                                    DownloadFileDao.updateStatus(DownloadTask(file, file.isDir), true)
                                } else {
                                    println("下载文件异常  ${file.path}   info.size:${FileUtils.formatFileSize(if (saveFile.exists()) saveFile.length() else { if (downloadingFile.exists()) downloadingFile.length() else -1 })}   info.size:${FileUtils.formatFileSize(file.size)}")
                                    println("\t  md5:${md5}   file.md5:${file.md5}   responseMD5:${it.responseMD5} \n")
                                    FileUtils.deleteFile(it.output)
                                    synchronized(waitingTasks) {
                                        waitingTasks.add(file)
                                        runingTasks.remove(file)
                                    }
                                }
                            }, {
                                when (it) {
                                    is SocketException,
                                    is SocketTimeoutException,
                                    is SSLHandshakeException,
                                    is SSLException,
                                    is UnknownHostException,
                                    is retrofit2.HttpException,
                                    is ProtocolException -> {
                                    }
                                    is IOException -> {
                                        println("${it::class.simpleName}: ${it.message}   ${file.path}")
                                    }
                                    else -> {
                                        LogUtils.d(it)
                                    }
                                }
                                synchronized(waitingTasks) {
                                    waitingTasks.add(file)
                                    runingTasks.remove(file)
                                }
                            })
                    synchronized(downloadDisposableMap) {
                        downloadDisposableMap.put(file.path, downloadDisposable!!)
                    }
                }
    }

    fun stop() {
        refreshTaskDisposable?.dispose()
        printTaskProgressDisposable?.dispose()

        waitingTasks.clear()
        runingTasks.clear()

        synchronized(downloadDisposableMap) {
            downloadDisposableMap.values.toTypedArray().forEach {
                if (!it.isDisposed) {
                    it.dispose()
                }
            }
        }
    }

}
