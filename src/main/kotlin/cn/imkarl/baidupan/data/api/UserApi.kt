package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.model.ResponseWrapper
import cn.imkarl.core.utils.EncodeUtils
import com.google.gson.JsonElement
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface UserApi {

    @GET("/")
    fun getBaiduUid(): Observable<Response<ResponseBody>>

    @GET("disk/cmsdata?do=client&channel=chunlei")
    fun getClientVersionInfos(
            @Query("t") timestamp: Long = System.currentTimeMillis(),
            @Query("clienttype") clientType: Int = 0,
            @Query("web") isWeb: Int = 1,
            @Query("logid") logid: String = EncodeUtils.encodeBase64("${System.currentTimeMillis()}${Random().nextDouble()}".toByteArray())!!
    ): Observable<JsonElement>


    @GET("https://wappass.baidu.com/wp/api/security/antireplaytoken?tpl=netdisk")
    fun getBaiduServerTime(
            @Query("v") timestamp: Long = System.currentTimeMillis()
    ): Observable<ResponseWrapper<JsonElement>>

    @GET("https://wappass.baidu.com/static/touch/js/login_d9bffc9.js")
    fun getBaiduRsaPublickKey(): Observable<String>

    @Headers(
            "Origin: https://wappass.baidu.com",
            "X-Requested-With: XMLHttpRequest"
    )
    @POST("https://wappass.baidu.com/wp/api/login?tt=1488201057879")
    @FormUrlEncoded
    fun login(
            @Field("username") username: String,
            @Field("password") password: String,
            @Field("verifycode") verifycode: String = "",
            @Field("vcodestr") vcodestr: String = "",
            @Field("action") action: String = "login",
            @Field("u") u: String = "https%3A%2F%2Fm.baidu.com%2Fusrprofile%3Fuid%3D1488201039395_134%23logined",
            @Field("tpl") wimn: String = "",
            @Field("tn") tn: String = "",
            @Field("pu") pu: String = "",
            @Field("ssid") ssid: String = "",
            @Field("from") from: String = "",
            @Field("bd_page_type") bd_page_type: String = "",
            @Field("uid") uid: String = "@",
            @Field("type") type: String = "",
            @Field("regtype") regtype: String = "",
            @Field("subpro") subpro: String = "wimn",
            @Field("adapter") adapter: String = "0",
            @Field("skin") skin: String = "default_v2",
            @Field("regist_mode") regist_mode: String = "",
            @Field("login_share_strategy") login_share_strategy: String = "",
            @Field("client") client: String = "",
            @Field("clientfrom") clientfrom: String = "",
            @Field("connect") connect: String = "0",
            @Field("bindToSmsLogin") bindToSmsLogin: String = "",
            @Field("isphone") isphone: String = "0",
            @Field("loginmerge") loginmerge: String = "1",
            @Field("countrycode") countrycode: String = "1",
            @Field("mobilenum") mobilenum: String = "undefined",
            @Field("servertime") servertime: String = "@",
            @Field("gid") gid: String = "gid",
            @Field("logLoginType") logLoginType: String = "wap_loginTouch"
    ): Observable<JsonElement>

}