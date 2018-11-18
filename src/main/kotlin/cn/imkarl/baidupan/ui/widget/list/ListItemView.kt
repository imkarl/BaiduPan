package cn.imkarl.baidupan.ui.widget.list

import java.awt.FlowLayout
import javax.swing.JPanel

/**
 * @author imkarl
 */
abstract class ListItemView<DATA>: JPanel(FlowLayout(FlowLayout.LEFT)) {
    abstract fun bindData(value: DATA, index: Int, isSelected: Boolean, cellHasFocus: Boolean)
}