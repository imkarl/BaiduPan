package cn.imkarl.core.utils

import cn.imkarl.baidupan.AppConfig
import java.io.*
import java.nio.channels.FileChannel
import java.text.DecimalFormat

/**
 * 文件相关工具类
 * @author imkarl
 */
object FileUtils {

    /**
     * 获取类所在的根目录
     */
    fun getClassRootFile(): File {
        val classLoader = FileUtils::class.java.classLoader
        if (AppConfig.isJarRun) {
            val codeSourcePath = FileUtils::class.java.protectionDomain.codeSource.location.path
            return File(codeSourcePath)
        }
        return File(classLoader.getResource("").path)
    }

    /**
     * 获取资源文件所在的根目录
     */
    fun getResourceRootFile(): File {
        if (AppConfig.isJarRun) {
            return getClassRootFile()
        }
        return File(getClassRootFile().parent, "resources")
    }

    /**
     * 获取用户默认根目录
     */
    fun getUserHomeRootFile(): File {
        return File(System.getProperty("user.home"), ".imkarl")
    }

    /**
     * 获取APP数据存储根目录
     */
    fun getAppStorageRootFile(): File {
        return File(getUserHomeRootFile(), AppConfig.appPackageName)
    }


    /**
     * 计算文件或文件夹的大小
     *
     * @return bytes, 字节大小
     */
    fun size(file: File): Long {
        if (!file.exists()) {
            return 0
        }

        if (file.isDirectory) {
            val children = file.listFiles() ?: return 0

            var sum: Long = 0
            for (child in children) {
                sum += size(child)
            }
            return sum
        } else {
            return file.length()
        }
    }

    /**
     * 计算文件夹总空间容量，单位byte
     */
    fun getTotalSpace(dir: File): Long {
        return if (dir.exists()) {
            dir.totalSpace
        } else {
            0
        }
    }

    /**
     * 计算文件夹剩余空间容量，单位byte
     */
    fun getFreeSpace(dir: File): Long {
        return if (dir.exists()) {
            dir.freeSpace
        } else {
            0
        }
    }

    /**
     * 重命名文件
     *
     * @return 如果目标文件已存在，则先删除（删除失败则返回false）
     */
    fun rename(from: File, to: File?): Boolean {
        if (!from.exists()) {
            LogUtils.w("File not found, 'from' is: $from")
            return false
        }
        if (to == null) {
            LogUtils.w("File not found, 'to' is: $to")
            return false
        }
        deleteFile(to)
        return from.renameTo(to)
    }

    /**
     * 创建文件夹（包括其父目录）
     *
     * @return 如果文件夹已存在，则直接返回true；如果该路径存在同名文件，则直接返回false
     * @see .createNewFile
     */
    fun mkdirs(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        return if (dir.exists()) {
            dir.isDirectory
        } else dir.mkdirs() || dir.isDirectory
    }

    /**
     * 创建文件（包括其父目录）
     *
     * @return 如果文件已存在，则删除重建
     * @see .mkdirs
     */
    fun createNewFile(file: File): Boolean {
        if (file.exists() && file.isFile) {
            deleteFile(file)
        }

        try {
            if (!mkdirs(file.parentFile)) {
                LogUtils.w("mkdirs 'NewFile' parent failed: " + file.parentFile.absolutePath)
                return false
            }
            return file.createNewFile()
        } catch (e: IOException) {
            LogUtils.w(e)
            return false
        }

    }


    /**
     * 删除文件（不支持文件夹）
     *
     * @return 不存在该文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    fun deleteFile(file: File): Boolean {
        if (!file.exists()) {
            //LogUtils.w("deleteFile 'file' not exist: "+file);
            return true
        }
        if (!file.isFile) {
            LogUtils.w("deleteFile 'file' is not a file")
            return true
        }
        return file.delete() || !file.exists()
    }

    /**
     * 删除文件夹（不支持文件）
     *
     * @return 不存在该文件夹，则返回true
     * @see .deleteFile 删除文件
     */
    fun deleteDir(dir: File): Boolean {
        if (!dir.exists()) {
            LogUtils.w("deleteDir 'dir' not exist: $dir")
            return true
        }
        if (!dir.isDirectory) {
            LogUtils.w("deleteDir 'dir' is not a directory")
            return true
        }

        val files = dir.listFiles()
        if (files == null) {
            LogUtils.w("deleteDir 'dir' not a readable directory: $dir")
            return false
        } else {
            for (file in files) {
                val delete: Boolean
                if (file.isDirectory) {
                    delete = deleteDir(file)
                } else {
                    delete = deleteFile(file)
                }
                if (!delete) {
                    LogUtils.w("delete failed : " + (if (file.isDirectory) "dir " else "file ") + file)
                    return false
                }
            }
        }

        return dir.delete() || !dir.exists()
    }


    /**
     * 复制文件或文件夹（包含空文件和空文件夹）
     *
     * @return 不存在源文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    fun copy(source: File, target: File?): Boolean {
        if (!source.exists()) {
            LogUtils.w("copy source not exists : $source to $target")
            return true
        }
        if (target == null) {
            LogUtils.w("copy target is NULL : $source to 'NULL'")
            return false
        }

        if (source.isDirectory) {
            val mkdir = mkdirs(target)
            if (!mkdir) {
                LogUtils.w("mkdir dir failed : $target")
                return false
            }

            val childs = source.listFiles()
            if (childs == null) {
                LogUtils.w("copy source not a readable directory: $source")
                return false
            }
            if (childs.size == 0) {
                return true
            }

            //LogUtils.i("copy dir "+source.getPath()+" to "+target.getPath());
            for (child in childs) {
                // 复制到对应的目标路径
                val copy = copy(child, File(target, child.name))
                if (!copy) {
                    LogUtils.w("copy failed : $source to $target")
                    return false
                }
            }
            return true
        } else if (source.isFile) {
            //LogUtils.i("copy file "+source.getPath()+" to "+target.getPath());
            val targetTempFile = File(target.parentFile, target.name + "." + System.currentTimeMillis())
            if (!copyFile(source, targetTempFile)) {
                LogUtils.w("copy failed : $source to $target")
                return false
            }
            return rename(targetTempFile, target)
        } else {
            LogUtils.w("copy source failed : " + source.path)
            return false
        }
    }

    /**
     * 复制文件
     *
     * @return 不存在源文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    private fun copyFile(source: File, target: File?): Boolean {
        if (!source.exists()) {
            LogUtils.i("copyFile source not exists : $source to $target")
            return true
        }
        if (target == null) {
            LogUtils.w("copyFile target is NULL : $source to 'NULL'")
            return false
        }

        if (source.length() == 0L) {
            return createNewFile(target)
        }

        val mkdir = mkdirs(target.parentFile)
        if (!mkdir) {
            LogUtils.w("mkdir dir failed : " + target.parentFile)
            return false
        }

        var result = false
        var fileChannel: FileChannel? = null
        var out: FileChannel? = null
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        try {
            inStream = FileInputStream(source)
            outStream = FileOutputStream(target)
            fileChannel = inStream.channel
            out = outStream.channel
            val length = fileChannel!!.transferTo(0, fileChannel.size(), out)
            if (length >= 0) {
                result = true
            } else {
                LogUtils.w("copyFile transferTo failed : $source to $target")
                result = false
            }
        } catch (e: IOException) {
            LogUtils.w(e)
        } finally {
            fileChannel.closeQuietly()
            out.closeQuietly()
            inStream.closeQuietly()
            outStream.closeQuietly()
        }
        return result
    }

    /**
     * 复制数据流到文件
     */
    fun copy(stream: InputStream, target: File): Boolean {
        val mkdir = FileUtils.mkdirs(target.parentFile)
        if (!mkdir) {
            return false
        }

        var result = false
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(target)
            IOUtils.copy(stream, fos)
            result = true
        } catch (e: IOException) {
            LogUtils.e(e)
        } finally {
            fos.closeQuietly()
        }
        return result
    }

    private val FILE_SIZE_KB = 1024L
    private val FILE_SIZE_MB = 1024 * FILE_SIZE_KB
    private val FILE_SIZE_GB = 1024 * FILE_SIZE_MB
    private val fileSizeFormat = DecimalFormat("0.00")

    fun formatFileSize(size: Long): String {
        if (size > FILE_SIZE_GB*1.2) {
            return "${fileSizeFormat.format((size*100/FILE_SIZE_GB)*0.01)}GB"
        }
        if (size > FILE_SIZE_MB*1.2) {
            return "${fileSizeFormat.format((size*100/FILE_SIZE_MB)*0.01)}MB"
        }
        if (size > FILE_SIZE_KB) {
            return "${fileSizeFormat.format((size*100/FILE_SIZE_KB)*0.01)}KB"
        }
        return "${fileSizeFormat.format(size)}B"
    }

}
