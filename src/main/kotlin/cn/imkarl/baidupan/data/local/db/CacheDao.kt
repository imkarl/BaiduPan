package cn.imkarl.baidupan.data.local.db

import cn.imkarl.baidupan.model.CacheInfo
import cn.imkarl.core.utils.EncryptUtils
import cn.imkarl.core.utils.FileUtils
import cn.imkarl.core.utils.JsonUtils
import java.io.File

/**
 * 缓存DAO
 * @author imkarl
 */
object CacheDao {

    fun findByKey(key: String): CacheInfo? {
        val cacheFile = getCacheFile(key)
        if (cacheFile.exists() && cacheFile.length() > 0) {
            val json = String(cacheFile.readBytes())
            return JsonUtils.fromJson(json, CacheInfo::class.java)
        }
        return null
    }

    fun deleteByKey(key: String): Boolean {
        return deleteCacheFile(key)
    }

    fun saveOrUpdate(key: String, data: String) {
        val cacheInfo = CacheInfo(data, System.currentTimeMillis())
        // create file
        val cacheFile = getCacheFile(key)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        cacheFile.createNewFile()
        // write data
        cacheFile.writeText(JsonUtils.toJson(cacheInfo))
    }

    private fun getCacheFile(key: String): File {
        val cacheFile = File(FileUtils.getAppStorageRootFile(), "cache/${EncryptUtils.md5(key)}")
        if (!cacheFile.parentFile.exists()) {
            cacheFile.parentFile.mkdirs()
        }
        return cacheFile
    }

    private fun deleteCacheFile(key: String): Boolean {
        val cacheFile = File(FileUtils.getAppStorageRootFile(), "cache/${EncryptUtils.md5(key)}")
        return FileUtils.deleteFile(cacheFile)
    }

}