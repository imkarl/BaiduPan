package cn.imkarl.baidupan.model

import com.google.gson.annotations.SerializedName

/**
 * 响应内容
 * @author imkarl
 */
data class ResponseWrapper<T>(
        @SerializedName("errno") var code: Int,
        @SerializedName("errmsg") var reason: String?,
        @SerializedName("data") var data: T
) {

    fun isSuccess(): Boolean {
        return code == 110000 || code == 0
    }

}