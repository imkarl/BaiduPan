package cn.imkarl.baidupan

import cn.imkarl.baidupan.data.local.SettingPrefs
import cn.imkarl.baidupan.data.local.db.DbService
import cn.imkarl.baidupan.ui.BaiduLoginFrame
import cn.imkarl.baidupan.ui.ChooserDirDialog
import cn.imkarl.baidupan.ui.MainPageFrame
import cn.imkarl.baidupan.ui.widget.dialog.Toast
import cn.imkarl.core.app.AppLifecycle
import cn.imkarl.core.download.Downloader
import cn.imkarl.core.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper
import java.util.concurrent.TimeUnit

/**
 * 程序入口
 * @author imkarl
 */
object Launch: AppLifecycle {

    @JvmStatic
    fun main(args: Array<String>) {
        launch()
    }

    override fun onStartup() {
        RxJavaPlugins.setErrorHandler { throwable ->
            LogUtils.e(throwable)
        }

        try {
            BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF()
        } catch (e: Exception) {
            LogUtils.e(e)
        }

        launchLogin()
    }

    override fun onShutdown() {
        DbService.stop()
        Downloader.stop()
    }

    private fun launchLogin() {
        if (SettingPrefs.bduss.isNullOrEmpty()) {
            BaiduLoginFrame.show {
                println("onLoginSuccess")
                launchChooserDownloadDir()
            }
        } else {
            launchChooserDownloadDir()
        }
    }

    /**
     * 选择下载目录
     */
    private fun launchChooserDownloadDir() {
        if (SettingPrefs.downloadDir.isNullOrEmpty()) {
            ChooserDirDialog.show { dir ->
                if (dir == null) {
                    Toast.show("请先选择下载文件保存路径~")
                    Observable.timer(2, TimeUnit.SECONDS)
                            .subscribe {
                                AppLifecycle.shutdown()
                            }
                } else {
                    SettingPrefs.downloadDir = dir.absolutePath
                    launchMainPage()
                }
            }
        } else {
            launchMainPage()
        }
    }

    /**
     * 跳转到主页
     */
    private fun launchMainPage() {
        // 启动DB服务
        DbService.start()

        // 打开主界面
        MainPageFrame.show()

        // 恢复下载任务
        Downloader.resumeHistoryDownloadTask()
    }

}
