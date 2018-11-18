package cn.imkarl.baidupan.ui

import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.ui.utils.ResourceUtils
import cn.imkarl.baidupan.ui.widget.list.TableAdapter
import cn.imkarl.baidupan.ui.widget.list.TableColumnView
import cn.imkarl.baidupan.ui.widget.list.TableView
import cn.imkarl.baidupan.ui.widget.onSingleClickListener
import cn.imkarl.core.utils.FileUtils
import java.awt.Color
import java.text.SimpleDateFormat
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * 文件列表
 * @author imkarl
 */
class ListFilesView: TableView<BaiduFile>() {

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    init {
        itemHeight = 32
        background = Color.WHITE
        adapter = TableAdapter(
                object : TableColumnView<BaiduFile>(" ☐", 30) {  // ☐ ☑
                    override fun createColumnView(): JComponent {
                        return JCheckBox()
                    }

                    override fun bindData(columnView: JComponent, rowData: BaiduFile, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        (columnView as JCheckBox).isSelected = isSelected
                    }
                },
                object : TableColumnView<BaiduFile>("文件名") {
                    override fun bindData(columnView: JComponent, rowData: BaiduFile, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        (columnView as JLabel).apply {
                            if (rowData.isDir) {
                                this.icon = ResourceUtils.getIcon("icon/folder.png", 22, 24)
                            } else {
                                this.icon = ResourceUtils.getIcon("icon/file.png", 24, 20)
                            }
                            this.text = rowData.filename
                        }
                    }
                },
                object : TableColumnView<BaiduFile>("修改时间", 148) {
                    override fun bindData(columnView: JComponent, rowData: BaiduFile, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        (columnView as JLabel).text = dateFormat.format(rowData.server_mtime * 1000)
                    }
                },
                object : TableColumnView<BaiduFile>("大小", 100) {
                    override fun bindData(columnView: JComponent, rowData: BaiduFile, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        (columnView as JLabel).text = if (rowData.size > 0) FileUtils.formatFileSize(rowData.size) else "-"
                    }
                }
        )
        getTableHeader().onSingleClickListener { event ->
            val column = getTableHeader().columnAtPoint(event.point)
            if (column == 0) {
                if (adapter?.getColumnView(column)?.columnName?.contains("☑") == true) {
                    adapter?.getColumnView(column)?.columnName = " ☐"
                    clearSelection()
                } else {
                    adapter?.getColumnView(column)?.columnName = " ☑"
                    selectAll()
                }
            }
        }
    }

}