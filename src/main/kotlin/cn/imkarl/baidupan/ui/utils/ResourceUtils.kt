package cn.imkarl.baidupan.ui.utils

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.core.utils.FileUtils
import java.awt.Dimension
import java.awt.Image
import java.awt.Toolkit
import java.io.File
import java.util.zip.ZipFile
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * 资源处理相关工具类
 * @author imkarl
 */
object ResourceUtils {

    private val imageCache by lazy { mutableMapOf<String, Image>() }

    fun getIcon(path: String, width: Int, height: Int): Icon {
        return getIcon(path, Dimension(width, height))
    }
    fun getIcon(path: String, size: Dimension? = null): Icon {
        return ImageIcon(getImage(path, size))
    }

    fun getImage(path: String, width: Int, height: Int): Image {
        return getImage(path, Dimension(width, height))
    }
    fun getImage(path: String, size: Dimension? = null): Image {
        val key = "${path}_${size?.width?:0}_${size?.height?:0}"
        var image = imageCache[key]
        if (image == null) {
            val parent = FileUtils.getResourceRootFile()
            if (AppConfig.isJarRun) {
                val zipFile = ZipFile(parent)
                val inputStream = zipFile.getInputStream(zipFile.getEntry(path))
                image = getImage(inputStream.readBytes())
            } else {
                image = getImage(File(parent, path).readBytes())
            }
            if (size != null) {
                image = image.getScaledInstance(size.width, size.height, Image.SCALE_DEFAULT)
            }
            imageCache[key] = image!!
        }
        return image
    }

    private fun getImage(data: ByteArray): Image {
        return Toolkit.getDefaultToolkit().createImage(data)
    }

}