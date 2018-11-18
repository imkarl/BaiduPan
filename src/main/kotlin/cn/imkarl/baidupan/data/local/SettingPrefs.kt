package cn.imkarl.baidupan.data.local

import cn.imkarl.baidupan.model.CookieItem
import cn.imkarl.core.utils.FileUtils
import cn.imkarl.core.utils.PropertiesHelper
import java.io.File

/**
 * 应用设置存储
 * @author imkarl
 */
object SettingPrefs {

    private val properties = PropertiesHelper(File(FileUtils.getAppStorageRootFile(), "prefs/settings.ini"))

    var baiduId by properties.field<String>("baiduId")

    var baiduCookies by properties.field<List<CookieItem>>("baidu_cookies")
    var bduss by properties.field<String>("bduss")

    var downloadDir by properties.field<String>("download_dir")

}
