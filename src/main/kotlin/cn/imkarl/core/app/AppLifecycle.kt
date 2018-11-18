package cn.imkarl.core.app

import cn.imkarl.core.utils.LogUtils

/**
 * 应用程序生命周期
 * @author imkarl
 */
interface AppLifecycle {

    fun launch() {
        try {
            Runtime.getRuntime().addShutdownHook(Thread {
                onShutdown()
            })
        } catch (e: Exception) {
            LogUtils.e(e)
        }
        onStartup()
    }

    companion object {
        fun shutdown() {
            println("AppLifecycle.stop()")
            System.exit(0)
        }
    }

    fun onStartup()
    fun onShutdown()
}