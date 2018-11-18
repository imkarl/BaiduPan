package cn.imkarl.baidupan.ui

import cn.imkarl.baidupan.model.FileDownloadProgress
import cn.imkarl.baidupan.ui.utils.ResourceUtils
import cn.imkarl.baidupan.ui.widget.dialog.Toast
import cn.imkarl.baidupan.ui.widget.list.TableAdapter
import cn.imkarl.baidupan.ui.widget.list.TableColumnView
import cn.imkarl.baidupan.ui.widget.list.TableView
import cn.imkarl.core.download.Downloader
import cn.imkarl.core.utils.FileUtils
import cn.imkarl.core.utils.LogUtils
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * 下载列表
 * @author imkarl
 */
class ListDownloadView: TableView<FileDownloadProgress>() {

    init {
        itemHeight = 60
        background = Color.WHITE
        adapter = TableAdapter(
                object : TableColumnView<FileDownloadProgress>("文件名") {
                    override fun bindData(columnView: JComponent, rowData: FileDownloadProgress, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        (columnView as JLabel).apply {
                            if (rowData.isDir) {
                                this.icon = ResourceUtils.getIcon("icon/folder.png", 22, 24)
                            } else {
                                this.icon = ResourceUtils.getIcon("icon/file.png", 24, 20)
                            }
                            this.text = rowData.filename
                        }
                        columnView.background = Color.WHITE
                        columnView.foreground = Color.BLACK
                    }
                },
                object : TableColumnView<FileDownloadProgress>("进度", 150) {
                    override fun bindData(columnView: JComponent, rowData: FileDownloadProgress, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        if (rowData.isSuccess) {
                            (columnView as JLabel).text = "下载完成"
                            columnView.foreground = Color(110, 200, 120)
                        } else {
                            (columnView as JLabel).text = FileUtils.formatFileSize(rowData.downloadSize) + " / " + FileUtils.formatFileSize(rowData.totalSize)
                            columnView.foreground = Color.BLACK
                        }
                        columnView.background = Color.WHITE
                    }
                },
                object : TableColumnView<FileDownloadProgress>("操作", 150) {
                    override fun bindData(columnView: JComponent, rowData: FileDownloadProgress, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                        if (rowData.isSuccess) {
                            columnView.isVisible = false
                            (columnView as JLabel).text = ""
                        } else {
                            columnView.isVisible = true
                            (columnView as JLabel).text = if (rowData.isStartup) "暂停" else "开始"
                        }
                        columnView.background = Color.WHITE
                        columnView.foreground = Color.BLACK
                    }
                }
        )
        onColumnSingleClick = { row, column, data ->
            if (column == 2) {
                onColumnClick(row, column, data)
            }
        }
        onColumnDoubleClick = { row, column, data ->
            if (column == 2) {
                onColumnClick(row, column, data)
            }
        }
    }

    private fun onColumnClick(row: Int, column: Int, data: FileDownloadProgress) {
        if (data.isSuccess) {
            return
        }

        data.isStartup = !data.isStartup
        Downloader.updateIsStartup(data, data.isStartup)
                .subscribe({
                    adapter?.notifyDataChanged()
                }, {
                    LogUtils.e(it)
                    Toast.show(it)
                })
    }

}