package cn.imkarl.baidupan.data.local.db

import cn.imkarl.baidupan.data.local.db.DbService.transaction
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.model.FileDownloadProgress
import org.h2.jdbc.JdbcSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement

/**
 * 下载任务 - Dao
 * @author imkarl
 */
object DownloadTaskDao : Table("t_download_task") {

    val path = varchar("path", 256).primaryKey()
    val filename = varchar("filename", 200)
    val totalSize = long("total_size")
    val isDir = bool("is_dir")
    val isSuccess = bool("is_success").default(false)
    val isStartup = bool("is_startup").default(false)

    private fun toStatement(task: FileDownloadProgress, statement: InsertStatement<Number>) {
        statement[path] = task.path
        statement[filename] = task.filename
        statement[totalSize] = task.totalSize
        statement[isDir] = task.isDir
        statement[isSuccess] = task.isSuccess
        statement[isStartup] = task.isStartup
    }
    private fun toBean(row: ResultRow): FileDownloadProgress {
        return FileDownloadProgress(row[path],
                row[filename],
                row[isDir],
                row[isSuccess],
                row[totalSize],
                -1,
                row[isStartup])
    }

    fun insert(tasks: Collection<FileDownloadProgress>): Int {
        return insert(*tasks.toTypedArray())
    }
    fun insert(vararg tasks: FileDownloadProgress): Int {
        var result = 0
        transaction {
            tasks.forEach { task ->
                try {
                    insert {
                        toStatement(task, it)
                    }
                    result++
                } catch (e: JdbcSQLException) {
                    if (e.message?.contains("Unique index or primary key violation") != true) {
                        throw e
                    }
                }
            }
        }
        return result
    }

    /**
     * 修改成功状态
     */
    fun updateStatus(task: FileDownloadProgress, success: Boolean): Int {
        var result = 0
        transaction {
            result = update({ path.eq(task.path) and isDir.eq(task.isDir) }) {
                it[isSuccess] = success
            }
        }
        return result
    }
    fun updateStatus(tasks: List<FileDownloadProgress>, success: Boolean): Int {
        var count = 0
        transaction {
            tasks.forEach { task ->
                val result = update({ path.eq(task.path) and isDir.eq(task.isDir) }) {
                    it[isSuccess] = success
                }
                if (result > 0) {
                    count += result
                }
            }
        }
        return count
    }

    /**
     * 修改开始状态
     */
    fun updateIsStartup(task: FileDownloadProgress, startup: Boolean): Int {
        var result = 0
        transaction {
            result = update({ path.eq(task.path) and isDir.eq(task.isDir) }) {
                it[isStartup] = startup
            }
        }
        return result
    }
    fun updateIsStartup(tasks: List<FileDownloadProgress>, startup: Boolean): Int {
        var count = 0
        transaction {
            tasks.forEach { task ->
                val result = update({ path.eq(task.path) and isDir.eq(task.isDir) }) {
                    it[isStartup] = startup
                }
                if (result > 0) {
                    count += result
                }
            }
        }
        return count
    }

    fun find(fileInfo: BaiduFile): FileDownloadProgress? {
        var result: FileDownloadProgress? = null
        transaction {
            select { path.eq(fileInfo.path) and isDir.eq(fileInfo.isDir) }
                    .limit(1, 0)
                    .forEach {
                        result = toBean(it)
                    }
        }
        return result
    }

    fun listAllByNotSuccess(): List<FileDownloadProgress>? {
        var result: List<ResultRow>? = null
        transaction {
            result = select { isSuccess.eq(false) and isStartup.eq(true) }.toList()
        }
        return result?.map { toBean(it) }
    }

    fun listAll(): List<FileDownloadProgress>? {
        var result: List<ResultRow>? = null
        transaction {
            result = selectAll().orderBy(path).toList()
        }
        return result?.map { toBean(it) }
    }

    fun count(where: SqlExpressionBuilder.()->Op<Boolean>): Int {
        return count(SqlExpressionBuilder.where())
    }
    fun count(where: Op<Boolean>? = null): Int {
        var result = 0
        transaction {
            result = (if (where==null) selectAll() else select(where)).count()
        }
        return result
    }

    fun total(): Int {
        var result = 0
        transaction {
            result = selectAll().count()
        }
        return result
    }

}