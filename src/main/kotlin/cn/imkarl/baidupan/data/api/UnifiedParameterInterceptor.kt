package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.baidupan.data.local.SettingPrefs
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 统一参数管理的拦截器
 * @author imkarl
 */
class UnifiedParameterInterceptor : Interceptor {

    companion object {
        private val USER_AEGNT = "User-Agent"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val reqBuilder = request.newBuilder()

        reqBuilder.removeHeader(USER_AEGNT)
        reqBuilder.addHeader(USER_AEGNT, AppConfig.userAgent)

        val newRequest = reqBuilder.build()
        val response = chain.proceed(newRequest)

        val cookies = response.header("Set-Cookie")
        cookies?.let {
            if (cookies.contains("BAIDUID")) {
                var baiduId = cookies.substring(cookies.indexOf("BAIDUID=") + "BAIDUID=".length)
                baiduId = baiduId.substring(0, baiduId.indexOf(";"))
                SettingPrefs.baiduId = baiduId
            }
        }

        return response
    }

}
