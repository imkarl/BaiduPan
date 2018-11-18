package cn.imkarl.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * JSON相关工具类
 * @author imkarl
 */
object JsonUtils {

    private val gson: Gson by lazy { createGsonBuilder().create() }
    private val gsonPretty: Gson by lazy { createGsonBuilder().setPrettyPrinting().create() }

    private fun createGsonBuilder(): GsonBuilder {
        return GsonBuilder()
                .registerTypeAdapter(Boolean::class.java, JsonDeserializer<Boolean> { json, _, _ ->
                    if (json.isJsonPrimitive) {
                        if (json.asJsonPrimitive.isBoolean) {
                            json.asJsonPrimitive.asBoolean
                        } else if (json.asJsonPrimitive.isNumber) {
                            json.asJsonPrimitive.asNumber.toDouble().toInt() == 1
                        } else if (json.asJsonPrimitive.isString) {
                            val str = json.asJsonPrimitive.asString
                            var result: Boolean
                            try {
                                result = str.toDouble().toInt() == 1
                            } catch (e: Exception) {
                                result = str?.toBoolean() ?: false
                            }
                            result
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                })
    }

    @JvmStatic
    fun <T> fromJson(json: String?, typeToken: TypeToken<T>): T {
        return fromJson(json, typeToken.type)
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, typeToken: TypeToken<T>): T {
        return fromJson(json, typeToken.type)
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: JsonElement?): T {
        return fromJson(json, object: TypeToken<T>() {})
    }

    @JvmStatic
    fun <T> fromJson(json: String?, typeOfT: Type): T {
        return gson.fromJson(json, typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, typeOfT: Type): T {
        return gson.fromJson(json, typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: String?, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }

    @JvmStatic
    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    @JvmStatic
    fun toJsonElement(src: Any?): JsonElement {
        return gson.toJsonTree(src)
    }

    @JvmStatic
    fun toJsonPrettyPrinting(src: Any): String {
        return gsonPretty.toJson(src)
    }

}
