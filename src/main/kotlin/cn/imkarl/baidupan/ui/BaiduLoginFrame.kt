package cn.imkarl.baidupan.ui

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.baidupan.data.local.SettingPrefs
import cn.imkarl.baidupan.model.CookieItem
import cn.imkarl.core.app.AppLifecycle
import cn.imkarl.core.utils.LogUtils
import cn.imkarl.core.utils.dismiss
import cn.imkarl.lib.browser.BrowserFactory
import com.teamdev.jxbrowser.chromium.dom.By
import com.teamdev.jxbrowser.chromium.dom.DOMElement
import com.teamdev.jxbrowser.chromium.dom.DOMNode
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent
import com.teamdev.jxbrowser.chromium.events.LoadAdapter
import com.teamdev.jxbrowser.chromium.swing.BrowserView
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.TimeUnit
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingUtilities

/**
 * 百度登录页
 * @author imkarl
 */
class BaiduLoginFrame: JFrame(AppConfig.appName+" - 登录") {

    companion object {
        val URL_LOGIN = "https://pan.baidu.com/"

        fun show(onLoginSuccess: (()->Unit)): BaiduLoginFrame {
            val browser = BaiduLoginFrame()
            browser.onLoginSuccess = {
                onLoginSuccess.invoke()
                browser.dismiss()
            }
            browser.isVisible = true

            browser.load()
            return browser
        }
    }

    val browserSize = Dimension(420, 500)

    private val browserView by lazy { BrowserView(BrowserFactory.createBrowser()) }
    private val browser by lazy { browserView.browser }
    private val loadingView by lazy { JLabel("loading...", JLabel.CENTER) }

    var onLoginSuccess: (()->Unit)? = null

    init {
        background = Color.WHITE
        layout = FlowLayout(FlowLayout.CENTER)
        size = Dimension(420, 500)
        setLocationRelativeTo(null)
        isResizable = false
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(p0: WindowEvent?) {
                if (SettingPrefs.bduss.isNullOrEmpty()) {
                    AppLifecycle.shutdown()
                }
            }
        })


        browserView.preferredSize = browserSize
        browserView.background = Color.WHITE
        browserView.isVisible = false

        loadingView.preferredSize = browserSize
        loadingView.background = Color.WHITE
        loadingView.isVisible = true
        add(loadingView)
        add(browserView)

        // 清空cookie
        browser.cookieStorage.deleteAll()

        // 监听页面加载情况
        browser.addLoadListener(object: LoadAdapter() {
            override fun onFinishLoadingFrame(event: FinishLoadingEvent) {
                if (event.isMainFrame) {
                    LogUtils.d("page load finish.")

                    val loginDiv = browser.document.findElement(By.className("login-sdk-v4"))

                    val bodyDiv = browser.document.findElement(By.tagName("body"))
                    bodyDiv.children.forEach {
                        if (it is DOMElement && it != loginDiv) {
                            it.hide()
                        }
                    }

                    Observable.timer(200, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe({ _ ->
                                loginDiv.findElements(By.className("tang-pass-footerBarULogin")).forEach {
                                    it.click()
                                }
                                loginDiv.findElements(By.tagName("a")).forEach {
                                    it.setAttribute("href", "javascript:void(0);")
                                }

                                Observable.timer(300, TimeUnit.MILLISECONDS)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.io())
                                        .subscribe({ _ ->
                                            LogUtils.d("page reset success.")

                                            val fullDiv = browser.document.createElement("div")
                                            val wrapperDiv = browser.document.createElement("div")
                                            fullDiv.setAttribute("style", "width:350px;height:100%;position:fixed;margin:0 auto;background:#FFFFFF;z-index:9999;")
                                            wrapperDiv.setAttribute("style", "width:340px;height:648px;position:relative;left:50%;top:50%;margin:-270px 0 0 -176px;")
                                            fullDiv.appendChild(wrapperDiv)
                                            bodyDiv.appendChild(fullDiv)

                                            loginDiv.parent.removeChild(loginDiv)
                                            wrapperDiv.appendChild(loginDiv)

                                            loginDiv.findElements(By.className("tang-pass-footerBarPhoenix")).forEach {
                                                it.setAttribute("style", "visibility:hidden;")
                                            }
                                            loginDiv.findElements(By.className("pass-reglink")).forEach {
                                                it.setAttribute("style", "visibility:hidden;")
                                            }
                                            loginDiv.findElements(By.className("pass-fgtpwd")).forEach {
                                                it.setAttribute("style", "visibility:hidden;")
                                            }

                                            showContent()
                                        }, { throwable ->
                                            LogUtils.e(throwable)
                                        })

                                showContent()
                            }, { throwable ->
                                LogUtils.e(throwable)
                            })
                } else {
                    if (event.validatedURL.contains("/api/?login")) {
                        // goto login
                        val allCookies = browser.cookieStorage.allCookies.map { CookieItem(it) }
                        SettingPrefs.baiduCookies = allCookies

                        var bduss: String? = null
                        allCookies.forEach { cookie ->
                            if (cookie.name == "BDUSS") {
                                bduss = cookie.value
                                SettingPrefs.bduss = bduss
                            }
                        }

                        if (!bduss.isNullOrEmpty()) {
                            SwingUtilities.invokeLater {
                                dispose()
                                onLoginSuccess?.invoke()
                            }
                        } else {
                            onLoginFailed()
                        }
                    }
                }
            }
        })
    }

    fun load() {
        showLoading()

        LogUtils.d("loadURL: $URL_LOGIN")
        browser.loadURL(URL_LOGIN)
    }

    fun showLoading() {
        browserView.isVisible = false
        loadingView.isVisible = true
    }
    fun showContent() {
        if (loadingView.isVisible) {
            browserView.isVisible = true
            Observable.timer(100, TimeUnit.MILLISECONDS)
                    .subscribe {
                        loadingView.isVisible = false
                        browserView.isVisible = false
                        browserView.isVisible = true
                    }
        }
    }

    private fun onLoginFailed() {
        println("onLoginFailed")
    }

    override fun dispose() {
        browser.dispose()
        super.dispose()
        isVisible = false
    }

    private fun DOMElement.hide() {
        this.removeAttribute("style")
        this.setAttribute("style", "display:none")
    }
    private fun DOMNode.removeSelf() {
        this.parent?.removeChild(this)
    }

}