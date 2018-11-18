package cn.imkarl.baidupan.model

import com.teamdev.jxbrowser.chromium.Cookie

data class CookieItem(
        val name: String,
        val value: String,
        val domain: String,
        val path: String,
        val creationTime: Long,
        val unixCreationTime: Long,
        val expirationTime: Long,
        val unixExpirationTime: Long,
        val isSecure: Boolean,
        val isHTTPOnly: Boolean,
        val isSession: Boolean
) {

    constructor(cookie: Cookie): this(cookie.name, cookie.value, cookie.domain, cookie.path,
            cookie.creationTime, cookie.unixCreationTime, cookie.expirationTime, cookie.unixExpirationTime,
            cookie.isSecure, cookie.isHTTPOnly, cookie.isSession)

}
