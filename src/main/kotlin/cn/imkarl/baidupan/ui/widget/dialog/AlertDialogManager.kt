package cn.imkarl.baidupan.ui.widget.dialog

import java.awt.Frame
import javax.swing.JOptionPane

/**
 * 弹窗管理器
 * @author imkarl
 */
object AlertDialogManager {

    fun showInputDialog(author: Frame, titile: String, onInputFinish: (String)->Unit) {
        showInputDialog(author, titile, onInputFinish, null)
    }
    fun showInputDialog(author: Frame, titile: String, onInputFinish: (String)->Unit, onInputCancel: (()->Unit)?) {
        val input = JOptionPane.showInputDialog(author, "", titile,JOptionPane.PLAIN_MESSAGE)
        if (input == null) {
            onInputCancel?.invoke()
        } else {
            onInputFinish.invoke(input)
        }
    }

}
