package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.data.local.SettingPrefs
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.model.BaiduFileList
import cn.imkarl.baidupan.model.ResponseWrapper
import com.google.gson.JsonElement
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BaiduApi {

    @GET("http://pan.baidu.com/api/list")
    fun listFiles(
            @Header("Cookie") cookies: String = "BDUSS="+SettingPrefs.bduss,
            @Query("order") order: String = "size",  // time  size
            @Query("desc") desc: Int = 1,
            @Query("showempty") showEmpty: Int = 0,
            @Query("web") isWeb: Int = 1,
            @Query("page") page: Int = 1,
            @Query("num") pageSize: Int = 1000,
            @Query("dir") dir: String = "/",
            @Query("t") randomNum: Double = Math.random(),
            @Query("channel") channel: String = "chunlei",
            @Query("app_id") appId: String = "250528",
            @Query("bdstoken") bdstoken: String = "c0e4aee7be57c180ac6b278378eed417",
            @Query("logid") logid: String = "MTUzNTAyMDM3ODA3MjAuMDg4ODc1NzcxMjM3NTQwOTg=",
            @Query("clienttype") clientType: Int = 0,
            @Query("startLogTime") startLogTime: Long = System.currentTimeMillis()
    ): Observable<ResponseWrapper<BaiduFileList>>

    @FormUrlEncoded
    @POST("https://pan.baidu.com/api/create?a=commit&channel=chunlei&web=1")
    fun createNewDir(
            @Header("Cookie") cookies: String = "BDUSS="+SettingPrefs.bduss,
            @Query("app_id") appId: String = "250528",
            @Query("bdstoken") bdstoken: String = "c0e4aee7be57c180ac6b278378eed417",
            @Query("logid") logid: String = "MTUzNTAyMDM3ODA3MjAuMDg4ODc1NzcxMjM3NTQwOTg=",
            @Query("clienttype") clientType: Int = 0,
            @Field("path") dir: String,
            @Field("isdir") isDir: Int = 1,
            @Field("block_list") blockList: String = "[]"
    ): Observable<ResponseWrapper<BaiduFile>>

    @FormUrlEncoded
    @POST("https://pan.baidu.com/api/filemanager?opera=delete")
    fun deleteFiles(
            @Header("Cookie") cookies: String = "BDUSS="+SettingPrefs.bduss,
            @Query("async") async: Int = 2,
            @Query("onnest") onnest: String = "fail",
            @Query("channel") channel: String = "chunlei",
            @Query("web") isWeb: Int = 1,
            @Query("app_id") appId: String = "250528",
            @Query("bdstoken") bdstoken: String = "c0e4aee7be57c180ac6b278378eed417",
            @Query("logid") logid: String = "MTUzNTAyMDM3ODA3MjAuMDg4ODc1NzcxMjM3NTQwOTg=",
            @Query("clienttype") clientType: Int = 0,
            @Field("filelist") paths: String
    ): Observable<ResponseWrapper<JsonElement>>


    @Streaming
    @GET("https://pcs.baidu.com/rest/2.0/pcs/file")
    fun download(
            @Header("Range") range: String? = null,
            @Header("Cookie") cookies: String = "BDUSS="+SettingPrefs.bduss,
            @Query("method") method: String = "download",
            @Query("app_id") appId: String = "250528",
            @Query("BDUSS") bduss: String = SettingPrefs.bduss ?: "",
            @Query("t") randomNum: Double = Math.random(),
            @Query("bdstoken") bdstoken: String = "c0e4aee7be57c180ac6b278378eed417",
            @Query("path") path: String
    ): Observable<Response<ResponseBody>>

}