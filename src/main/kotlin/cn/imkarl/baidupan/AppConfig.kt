package cn.imkarl.baidupan

/**
 * 应用程序配置
 * @author imkarl
 */
object AppConfig {

    val appName = "百度快盘"
    val appPackageName = "cn.imkarl.baidupan"
    val baseUrl = "https://pan.baidu.com/"
    val userAgent = "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-CN; MIX 2 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.1.1.991 Mobile Safari/537.36"
    val deviceId = "BDIMXV2%2DO%5FF7840B776940430D9AB7A059C62B4E89%2DC%5F0%2DD%5FS2DKNXAG901226M%2DM%5FF832E4761C17%2DV%5F1C61EEB6"

    val ignoreHostnameVerifier = true
    val showHttpLogger = false
    val showSqlLogger = false
    val isJarRun = AppConfig::class.java.classLoader.getResource(".") == null

}