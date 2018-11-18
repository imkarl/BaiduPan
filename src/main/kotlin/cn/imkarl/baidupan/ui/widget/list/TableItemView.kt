package cn.imkarl.baidupan.ui.widget.list

import java.awt.Color
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

/**
 * 表格列
 * @author imkarl
 */
abstract class TableColumnView<DATA>(var columnName: String, var columnWidth: Int = -1): TableCellRenderer {
    companion object {
        private val backgroundColorByDefault = Color.WHITE
        private val backgroundColorBySelected = Color(220, 220, 220)
        private val backgroundColorByFocus = Color(235, 235, 235)
        private val foregroundByDefault = Color.BLACK
        private val foregroundBySelected = Color.BLACK
    }

    private val columnView by lazy { createColumnView() }

    open fun createColumnView(): JComponent {
        return JLabel()
    }

    final override fun getTableCellRendererComponent(
            table: JTable?, value: Any?,
            isSelected: Boolean, hasFocus: Boolean,
            row: Int, column: Int
    ): JComponent {
        if (table != null && value != null) {
            columnView.isOpaque = true
            columnView.background = if (isSelected) backgroundColorBySelected else backgroundColorByDefault
            columnView.foreground = if (isSelected) foregroundBySelected else foregroundByDefault
            if (!isSelected && hasFocus) {
                columnView.background = backgroundColorByFocus
            }
            @Suppress("UNCHECKED_CAST")
            bindData(columnView, value as DATA, isSelected, hasFocus, row, column)
        }
        return columnView
    }

    open fun bindData(columnView: JComponent, rowData: DATA, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
        (columnView as JLabel).text = column.toString()
    }

}
