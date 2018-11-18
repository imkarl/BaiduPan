package cn.imkarl.baidupan.data.repository

import cn.imkarl.baidupan.data.api.BaiduApi
import cn.imkarl.baidupan.data.local.SettingPrefs
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.core.download.DownloadProgress
import cn.imkarl.core.download.DownloadUtils
import cn.imkarl.core.download.ResponseToFileObservable
import cn.imkarl.core.utils.FileUtils
import cn.imkarl.core.utils.JsonUtils
import io.reactivex.Observable
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 百度Repository
 * @author imkarl
 */
object BaiduRepository: Repository() {
    private val api by lazy { createApi(BaiduApi::class.java) }
    private var isRequestListFiles = false

    fun listFiles(dir: String = "/", page: Int = 1, pageSize: Int = 1000): Observable<ArrayList<BaiduFile>> {
        return Observable.just(dir)
                .flatMap { _ ->
                    synchronized(BaiduRepository::class) {
                        if (isRequestListFiles) {
                            return@flatMap Observable.timer(10, TimeUnit.MILLISECONDS)
                                    .flatMap { listFiles(dir, page, pageSize) }
                        }
                    }
                    return@flatMap Observable.timer(10, TimeUnit.MILLISECONDS)
                            .flatMap { listFiles2(dir, page, pageSize) }
                            .doOnSubscribe {
                                synchronized(BaiduRepository::class) {
                                    isRequestListFiles = true
                                }
                            }
                            .doFinally {
                                synchronized(BaiduRepository::class) {
                                    isRequestListFiles = false
                                }
                            }
                }
                .retry(10)
    }
    fun listFiles2(dir: String = "/", page: Int = 1, pageSize: Int = 1000): Observable<ArrayList<BaiduFile>> {
        val curPage = if (page < 1) { 1 } else { page }
        return api.listFiles(dir=dir, page=curPage, pageSize=pageSize, cookies=allCookiesStr())
                .compose(unwrap())
                .compose(process())
                .map {
                    if ("/".equals(dir) && curPage == 1) {
                        // 将apps文件夹移到首位
                        var index = it.list.size-1
                        while (index >= 0) {
                            if ("apps".equals(it.list[index].filename)) {
                                it.list[index].filename = "我的应用数据"
                                val item = it.list.removeAt(index)
                                it.list.add(0, item)
                                index = -1
                            }
                            index--
                        }
                    }
                    it.list
                }
                .compose(cache("listFiles_${dir}_${page}_${pageSize}", TimeUnit.DAYS.toMillis(10)))
    }

    private fun deleteCacheByListFiles(dir: String = "/", page: Int = 1, pageSize: Int = 1000) {
        var parentDir = dir
        if (parentDir.lastIndexOf("/") > 0) {
            parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"))
        }
        deleteCache("listFiles_${parentDir}_${page}_${pageSize}")
    }

    fun createNewDir(dir: String): Observable<BaiduFile> {
        return api.createNewDir(dir=dir)
                .compose(unwrap())
                .compose(process())
    }

    fun deleteFiles(vararg paths: String): Observable<Boolean> {
        if (paths.isEmpty()) {
            return Observable.just(true)
        }
        return api.deleteFiles(paths=JsonUtils.toJson(paths.asList()), async=1)
                .compose(unwrap())
                .compose(process())
                .map {
                    paths.forEach { path ->
                        deleteCacheByListFiles(path)
                    }
                }
                .map { true }
                .delay(50, TimeUnit.MILLISECONDS)
    }

    fun download(file: BaiduFile, output: File): Observable<DownloadProgress> {
        val range = if (DownloadUtils.isAvailableFile(output)) DownloadUtils.buildRangeHeader(FileUtils.size(output)) else null
        return api.download(path=file.path, range=range)
                .compose(process())
                .flatMap { response ->
                    // 将响应内容写入到本地文件
                    ResponseToFileObservable.create(response, output)
                            .flatMap { progress ->
                                if (progress.isSuccess) {
                                    progress.responseMD5 = DownloadUtils.getMD5(response.headers()) ?: ""
                                }
                                Observable.just(progress)
                            }
                }
    }

    private fun allCookiesStr(): String {
        val allCookies = StringBuilder()
        SettingPrefs.baiduCookies?.forEach {
            allCookies.append(it.name).append('=').append(it.value).append(';').append(' ')
        }
        if (allCookies.isNotEmpty()) {
            allCookies.delete(allCookies.length-2, allCookies.length)
        }
        return allCookies.toString()
    }

}
