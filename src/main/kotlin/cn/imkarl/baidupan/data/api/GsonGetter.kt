package cn.imkarl.baidupan.data.api

import cn.imkarl.baidupan.model.ResponseWrapper
import cn.imkarl.core.utils.JsonUtils
import com.google.gson.*
import java.lang.reflect.Type

object GsonGetter {

    val gson: Gson by lazy {
        GsonBuilder()
                .registerTypeAdapter(ResponseWrapper::class.java, object : JsonDeserializer<ResponseWrapper<*>> {
                    @Throws(JsonParseException::class)
                    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ResponseWrapper<*> {
                        return JsonUtils.fromJson(handler(json), typeOfT)
                    }

                    private fun handler(json: JsonElement): JsonElement {
                        if (json.isJsonObject && json.asJsonObject.has("errno")) {
                            val errno = json.asJsonObject.get("errno")
                            val errmsg = json.asJsonObject.get("errmsg")
                            json.asJsonObject.remove("errno")
                            json.asJsonObject.remove("errmsg")

                            val result = JsonObject()
                            result.add("errno", errno)
                            result.add("errmsg", errmsg)
                            result.add("data", json)
                            return result
                        }
                        return json
                    }
                })
                .create()
    }

}