package cn.imkarl.core.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object StringConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(type: Type?,
                                       annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, *>? {
        if (isStringType(type)) {
            return Converter<ResponseBody, String> { value -> value.string() }
        }
        return null
    }

    private fun isStringType(type: Type?): Boolean {
        try {
            if (type != null && (type as Class<*>).equals(String::class.java)) {
                return true
            }
        } catch (e: Exception) {
        }
        return false
    }

}
