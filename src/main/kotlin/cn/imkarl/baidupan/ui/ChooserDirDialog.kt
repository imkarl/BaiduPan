package cn.imkarl.baidupan.ui

import org.jb2011.lnf.beautyeye.ch20_filechooser.BEFileChooserUICross
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JLabel

/**
 * 选择文件夹的弹窗
 * @author imkarl
 */
object ChooserDirDialog {

    fun show(onChooseFile: (File?) -> Unit) {
        val fileChooser = JFileChooser()
        BEFileChooserUICross.createUI(fileChooser)
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fileChooser.showDialog(JLabel(), "选择")
        val dir = fileChooser.selectedFile
        if (dir != null) {
            onChooseFile.invoke(dir)
        } else {
            onChooseFile.invoke(null)
        }
    }

}