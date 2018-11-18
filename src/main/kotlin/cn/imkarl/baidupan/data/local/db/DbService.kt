package cn.imkarl.baidupan.data.local.db

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.core.utils.LogUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * 数据库服务
 * @author imkarl
 */
object DbService {

    private val tabls = arrayOf(
            DownloadTaskDao,
            DownloadFileDao
    )

    fun start() {
        Database.connect("jdbc:h2:~/.imkarl/${AppConfig.appPackageName}/db/baidupan_download", driver = "org.h2.Driver")
        transaction {
            logger.addLogger(StdOutSqlLogger)

            tabls.forEach {
                try {
                    SchemaUtils.create(it)
                } catch (e: Exception) {
                    LogUtils.e(e)
                }
            }
        }

        LogUtils.i("DbService start success.")
    }

    fun stop() {
        // do somthing
    }

    fun <T> transaction(statement: Transaction.() -> T): T {
        return transaction(null) {
            if (AppConfig.showSqlLogger) {
                logger.addLogger(StdOutSqlLogger)
            }
            statement.invoke(this)
        }
    }

}
