package cn.imkarl.baidupan.ui.widget.list

import cn.imkarl.baidupan.ui.widget.setOnClickListener
import cn.imkarl.core.utils.LogUtils
import java.awt.Color
import java.awt.Point
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

/**
 * 表格控件
 * @author imkarl
 */
open class TableView<DATA>: JScrollPane(JTable()) {

    private val table by lazy { viewport.view as JTable }
    private val defaultModel by lazy { DefaultTableModel() }
    var onColumnSingleClick: ((Int, Int, DATA)->Unit)? = null
    var onColumnDoubleClick: ((Int, Int, DATA)->Unit)? = null
    var onSelectChanged: (()->Unit)? = null

    private var lastIsClcik = false

    init {
        // 不可拖动
        table.tableHeader.reorderingAllowed = false
        // 监听点击事件
        table.setOnClickListener({ event ->
            val row = table.rowAtPoint(event.point)
            val column = table.columnAtPoint(event.point)
            val data = table.getValueAt(row, column)

            @Suppress("UNCHECKED_CAST")
            onColumnSingleClick?.invoke(row, column, data as DATA)

            if (lastIsClcik) {
                setRowSelected(row, false)
                LogUtils.d("removeSelection")
            }
            lastIsClcik = true
        }, { event ->
            val row = table.rowAtPoint(event.point)
            val column = table.columnAtPoint(event.point)
            val data = table.getValueAt(row, column)
            @Suppress("UNCHECKED_CAST")
            onColumnDoubleClick?.invoke(row, column, data as DATA)
        })
        table.selectionModel.addListSelectionListener { event ->
            onSelectChanged?.invoke()
            lastIsClcik = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    var adapter: TableAdapter<DATA>?
        get() { return if (table.model is TableAdapter<*>) table.model as TableAdapter<DATA> else null }
        set(value) {
            if (value != null) {
                table.model = value
                value.table = table
            } else {
                table.model = defaultModel
            }
        }

    var itemHeight: Int
        get() { return table.rowHeight }
        set(value) { table.rowHeight = value }

    override fun setBackground(color: Color?) {
        super.setBackground(color)
        viewport?.background = color
        viewport?.view?.background = color
    }

    fun getSelectedRows() = table.selectedRows

    fun getSelectedData() = getSelectedRows().map { table.getValueAt(it, 0) as DATA }

    fun isRowSelected(row: Int) = table.isRowSelected(row)

    fun setRowSelected(row: Int, selected: Boolean) {
        if (selected) {
            table.setRowSelectionInterval(row, 0)
        } else {
            table.removeRowSelectionInterval(row, 0)
        }
    }

    fun clearSelection() {
        table.clearSelection()
    }

    fun selectAll() {
        table.selectAll()
    }

    fun getTableHeader() = table.tableHeader

    fun rowAtPoint(point: Point): Int {
        return table.rowAtPoint(point)
    }
    fun columnAtPoint(point: Point): Int {
        return table.columnAtPoint(point)
    }

}
