package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.core.retrofit.StringConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


/**
 * API管理
 * @author imkarl
 */
object ApiManager {

    private val HTTP_CONNECT_TIMEOUT: Long = 10
    private val HTTP_READ_TIMEOUT: Long = 10
    private val HTTP_WRITE_TIMEOUT: Long = 10

    private val retrofit: Retrofit by lazy {
        val httpClient = OkHttpClient.Builder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(UnifiedParameterInterceptor())
                .addInterceptor(LoggingInterceptor())
                .let {
                    if (AppConfig.ignoreHostnameVerifier) {
                        it.sslSocketFactory(getSSLSocketFactory(), getTrustManager())
                        it.hostnameVerifier(getHostnameVerifier())
                    }
                    return@let it
                }
                .build()

        return@lazy Retrofit.Builder()
                .baseUrl(AppConfig.baseUrl)
                .client(httpClient)
                .addConverterFactory(StringConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(GsonGetter.gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private fun getSSLSocketFactory(): SSLSocketFactory {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(getTrustManager()), SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
    private fun getTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }
    }
    private fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }


    fun <T> createApi(apiClass: Class<T>): T {
        return retrofit.create(apiClass)
    }

}
