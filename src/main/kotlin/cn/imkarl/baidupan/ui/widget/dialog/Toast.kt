package cn.imkarl.baidupan.ui.widget.dialog

import cn.imkarl.core.utils.description
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.awt.Color
import java.awt.Dimension
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * 吐司
 * @author imkarl
 */
object Toast {

    private val dialog by lazy { JDialog(JFrame(), "ToastDialog", false) }
    private val label by lazy { JTextPane() }

    init {
        SwingUtilities.invokeLater {
            // 对齐方式
            label.alignmentX = JTextPane.CENTER_ALIGNMENT
            label.alignmentY = JTextPane.CENTER_ALIGNMENT
            val bSet = SimpleAttributeSet()
            StyleConstants.setAlignment(bSet, StyleConstants.ALIGN_CENTER)
            label.styledDocument.setParagraphAttributes(0, 104, bSet, false)

            // 各种状态
            label.isEnabled = false
            label.isEditable = false
            label.isOpaque = true
            label.background = Color.WHITE
            label.disabledTextColor = Color.BLACK
            label.selectedTextColor = Color.BLACK
            label.border = BorderFactory.createEtchedBorder()
            dialog.add(label)

            // 无边框
            dialog.isUndecorated = true
            // 置顶
            if (dialog.isAlwaysOnTopSupported) {
                dialog.isAlwaysOnTop = true
            }
        }
    }

    fun show(throwable: Throwable?) {
        show(throwable.description)
    }

    fun show(message: String) {
        SwingUtilities.invokeLater {
            label.text = message

            // 计算宽高
            val itemSize = label.font.size + 2
            val lineLength = Math.min(message.length, 40)
            val lineCount = ((message.length - 1) / lineLength) + 1
            label.preferredSize = Dimension(lineLength * (itemSize - 2) + 8, lineCount * itemSize + 12)
            dialog.pack()

            // 屏幕居中显示
            dialog.setLocationRelativeTo(null)

            dialog.isVisible = true
            dialog.toFront()

            Observable.timer(2500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe {
                        dialog.isVisible = false
                    }
        }
    }

}
