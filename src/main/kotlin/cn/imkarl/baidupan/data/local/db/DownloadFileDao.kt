package cn.imkarl.baidupan.data.local.db

import cn.imkarl.baidupan.data.local.db.DbService.transaction
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.model.DownloadTask
import org.h2.jdbc.JdbcSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement

/**
 * 下载文件 - Dao
 * @author imkarl
 */
object DownloadFileDao : Table("t_download") {

    val path = varchar("path", 256).primaryKey()

    val fs_id = varchar("fs_id", 20)
    val server_ctime = long("server_ctime")
    val server_mtime = long("server_mtime")
    val local_ctime = long("local_ctime")
    val local_mtime = long("local_mtime")
    val filename = varchar("filename", 200)
    val size = long("size")
    val md5 = varchar("md5", 32).nullable()
    val isDir = bool("is_dir")
    val isSuccess = bool("is_success").default(false)

    private fun toStatement(task: DownloadTask, statement: InsertStatement<Number>) {
        statement[path] = task.fileInfo.path
        statement[fs_id] = task.fileInfo.fs_id
        statement[server_ctime] = task.fileInfo.server_ctime
        statement[server_mtime] = task.fileInfo.server_mtime
        statement[local_ctime] = task.fileInfo.local_ctime
        statement[local_mtime] = task.fileInfo.local_mtime
        statement[filename] = task.fileInfo.filename
        statement[size] = task.fileInfo.size
        statement[md5] = task.fileInfo.md5
        statement[isDir] = task.fileInfo.isDir
        statement[isSuccess] = task.isSuccess
    }
    private fun toBean(row: ResultRow): DownloadTask {
        return DownloadTask(BaiduFile(
                path=row[path],
                fs_id=row[fs_id],
                server_ctime=row[server_ctime],
                server_mtime=row[server_mtime],
                local_ctime=row[local_ctime],
                local_mtime=row[local_mtime],
                filename=row[filename],
                size=row[size],
                md5=row[md5],
                isDir=row[isDir]
                ), row[isDir], row[isSuccess])
    }

    fun insert(tasks: Collection<DownloadTask>): Int {
        return insert(*tasks.toTypedArray())
    }
    fun insert(vararg tasks: DownloadTask): Int {
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
    fun updateStatus(task: DownloadTask, success: Boolean): Int {
        var result = 0
        transaction {
            result = update({ path.eq(task.fileInfo.path) and isDir.eq(task.isDir) }) {
                it[isSuccess] = success
            }
        }
        return result
    }
    fun updateStatus(tasks: List<DownloadTask>, success: Boolean): Int {
        var count = 0
        transaction {
            tasks.forEach { task ->
                val result = update({ path.eq(task.fileInfo.path) and isDir.eq(task.isDir) }) {
                    it[isSuccess] = success
                }
                if (result > 0) {
                    count += result
                }
            }
        }
        return count
    }

    fun find(fileInfo: BaiduFile): DownloadTask? {
        var result: DownloadTask? = null
        transaction {
            select { path.eq(fileInfo.path) and isDir.eq(fileInfo.isDir) }
                    .limit(1, 0)
                    .forEach {
                        result = toBean(it)
                    }
        }
        return result
    }

    fun listAllByNotSuccess(): List<DownloadTask>? {
        var result: List<ResultRow>? = null
        transaction {
            result = select { isSuccess.eq(false) }.orderBy(size).toList()
        }
        return result?.map { toBean(it) }
    }

    fun listAll(): List<DownloadTask>? {
        var result: List<ResultRow>? = null
        transaction {
            result = selectAll().toList()
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