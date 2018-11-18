package cn.imkarl.baidupan.ui.widget.list

import javax.swing.JTable
import javax.swing.event.TableModelEvent
import javax.swing.table.AbstractTableModel

/**
 * 表格适配器
 * @author imkarl
 */
class TableAdapter<DATA>(private vararg val columnViews: TableColumnView<DATA>)
    : AbstractTableModel() {

    internal var table: JTable? = null
        set(value) {
            field = value
            notifyColumnChanged()
        }
    private val modelEvent by lazy { TableModelEvent(this) }
    private val items by lazy { mutableListOf<DATA>() }

    override fun getRowCount(): Int {
        return items.size
    }

    fun getColumnView(columnIndex: Int): TableColumnView<DATA> {
        return columnViews[columnIndex]
    }

    override fun getColumnName(columnIndex: Int): String {
        return columnViews[columnIndex].columnName
    }

    override fun getColumnCount(): Int {
        return columnViews.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): DATA {
        return items[rowIndex]
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getColumnClass(var1: Int): Class<*> {
        return Any::class.java
    }

    override fun setValueAt(var1: Any, var2: Int, var3: Int) {}

    fun notifyDataChanged() {
        fireTableChanged(modelEvent)
        notifyColumnChanged()
    }

    private fun notifyColumnChanged() {
        table?.apply {
            for (i in 0 until columnCount) {
                val column = columnModel.getColumn(i)
                val columnIndex = column.modelIndex
                column.cellRenderer = columnViews[columnIndex]

                val columnWidth = columnViews[columnIndex].columnWidth
                if (columnWidth >= 0) {
                    column.preferredWidth = columnWidth
                    column.minWidth = columnWidth
                    column.maxWidth = columnWidth
                }
            }
        }
    }

    fun add(data: DATA) {
        items.add(data)
    }

    fun addAll(data: Collection<DATA>) {
        items.addAll(data)
    }

    fun remove(data: DATA) {
        items.remove(data)
    }

    fun clear() {
        items.clear()
    }

}
