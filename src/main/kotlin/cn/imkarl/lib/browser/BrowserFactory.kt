package cn.imkarl.lib.browser

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.core.utils.FileUtils
import com.teamdev.jxbrowser.chromium.Browser
import com.teamdev.jxbrowser.chromium.LoggerProvider
import com.teamdev.jxbrowser.chromium.ba
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigInteger
import java.util.logging.Level

object BrowserFactory {

    fun createBrowser(): Browser {
        closeLogger()
        disableLicenses()
        copyTeamdevLicenses()
        return Browser()
    }

    private fun closeLogger() {
        LoggerProvider.setLevel(Level.OFF)
    }

    private fun disableLicenses() {
        try {
            val e = ba::class.java.getDeclaredField("e")
            e.isAccessible = true
            val f = ba::class.java.getDeclaredField("f")
            f.isAccessible = true
            val modifersField = Field::class.java.getDeclaredField("modifiers")
            modifersField.isAccessible = true
            modifersField.setInt(e, e.modifiers and Modifier.FINAL.inv())
            modifersField.setInt(f, f.modifiers and Modifier.FINAL.inv())
            e.set(null, BigInteger("1"))
            f.set(null, BigInteger("1"))
            modifersField.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyTeamdevLicenses() {
        if (!AppConfig.isJarRun) {
            val srcDir = FileUtils.getResourceRootFile()
            val outDir = FileUtils.getClassRootFile()
            val fileName = "META-INF/teamdev.licenses"
            val srcFile = File(srcDir, fileName)
            if (srcFile.exists()) {
                srcFile.renameTo(File(outDir, fileName))
            }
        }
    }

}
