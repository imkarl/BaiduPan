package cn.imkarl.baidupan.ui

import cn.imkarl.baidupan.AppConfig
import cn.imkarl.baidupan.data.repository.BaiduRepository
import cn.imkarl.baidupan.model.BaiduFile
import cn.imkarl.baidupan.ui.widget.dialog.AlertDialogManager
import cn.imkarl.baidupan.ui.widget.dialog.Toast
import cn.imkarl.baidupan.ui.widget.onClickListener
import cn.imkarl.baidupan.ui.widget.onDoubleClickListener
import cn.imkarl.baidupan.ui.widget.onSingleClickListener
import cn.imkarl.baidupan.utils.BaiduUtils
import cn.imkarl.core.download.Downloader
import cn.imkarl.core.utils.FileUtils
import cn.imkarl.core.utils.LogUtils
import cn.imkarl.core.utils.description
import cn.imkarl.core.utils.sumByLong
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.awt.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * 主页
 * @author imkarl
 */
class MainPageFrame: JFrame(AppConfig.appName+" - 极速下载") {

    companion object {
        fun show(): MainPageFrame {
            val frame = MainPageFrame()
            frame.isVisible = true
            return frame
        }
    }

    private val menuView: JPanel
    private val navigationView: JPanel
    private val listFilesView: ListFilesView
    private val listDownloadView: ListDownloadView
    private val btnDownload: JButton
    private val btnDelete: JButton
    private val progress: JProgressBar
    private val labelNetspeed: JLabel

    private var refreshDownloadProgressDisposable: Disposable? = null

    private var currentDir = "/"

    init {
        background = Color.WHITE
        layout = BorderLayout(2, 1)
        size = Dimension(950, 650)
        minimumSize = Dimension(600, 400)
        setLocationRelativeTo(null)
        isResizable = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        val cardLayout = CardLayout()
        val contentPanel = JPanel(cardLayout)
        add(contentPanel, BorderLayout.CENTER)

        // 文件列表
        val listFilesPanel = JPanel(BorderLayout())
        val topPanel = Box.createVerticalBox()
        listFilesPanel.add(topPanel, BorderLayout.NORTH)
        contentPanel.add("listFilesPanel", listFilesPanel)

        // 下载列表
        val listDownloadPanel = JPanel(BorderLayout(2, 2))

        val topProgressPanel = JPanel(BorderLayout(10, 10))
        topProgressPanel.border = EmptyBorder(10, 10, 10, 10)
        topProgressPanel.background = Color.WHITE
        val labelProgressTitle = JLabel("下载总进度")
        labelProgressTitle.preferredSize = Dimension(60, 10)
        labelProgressTitle.horizontalAlignment = JLabel.RIGHT
        topProgressPanel.add(labelProgressTitle, BorderLayout.WEST)
        progress = JProgressBar(JProgressBar.HORIZONTAL, 0, 1000)
        topProgressPanel.add(progress, BorderLayout.CENTER)
        labelNetspeed = JLabel()
        labelNetspeed.preferredSize = Dimension(150, 10)
        topProgressPanel.add(labelNetspeed, BorderLayout.EAST)
        listDownloadPanel.add(topProgressPanel, BorderLayout.NORTH)

        contentPanel.add("listDownloadPanel", listDownloadPanel)
        cardLayout.show(contentPanel, "listFilesPanel")

        // 操作栏
        val operationPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        operationPanel.background = Color.WHITE

        val btnCreateNewDir = JButton("新建文件夹")
        operationPanel.add(btnCreateNewDir)

        btnDownload = JButton("下载")
        btnDownload.isVisible = false
        operationPanel.add(btnDownload)

        btnDelete = JButton("删除")
        btnDelete.isVisible = false
        operationPanel.add(btnDelete)

        topPanel.add(operationPanel)

        // 导航栏
        navigationView = JPanel()
        navigationView.layout = FlowLayout(FlowLayout.LEFT)
        navigationView.border = EmptyBorder(4, 0, 0, 0)
        navigationView.preferredSize = Dimension(0, 30)
        navigationView.background = Color.WHITE
        topPanel.add(navigationView)


        // 目录列表
        listFilesView = ListFilesView()
        listFilesView.onSelectChanged = {
            changeBtnsVisible()
        }
        listFilesView.onColumnDoubleClick = { _, _, data ->
            if (data.isDir) {
                changeCurrentDir(data.path)
            }
        }
        listFilesPanel.add(listFilesView, BorderLayout.CENTER)


        // 下载列表
        listDownloadView = ListDownloadView()
        listDownloadPanel.add(listDownloadView, BorderLayout.CENTER)

        btnDownload.addActionListener { _ ->
            downloadFile(*listFilesView.getSelectedData().toTypedArray())
        }
        btnDelete.addActionListener { _ ->
            deleteFile(*listFilesView.getSelectedData().toTypedArray())
        }
        btnCreateNewDir.addActionListener { _ ->
            createNewDir()
        }

        // 左侧菜单
        val bgMenu = Color(245, 245, 245)
        menuView = JPanel(FlowLayout(FlowLayout.CENTER))
        menuView.background = bgMenu
        menuView.preferredSize = Dimension(150, 0)

        val totalSizePanel = JPanel()
        totalSizePanel.preferredSize = Dimension(150, 80)
        totalSizePanel.background = bgMenu
        menuView.add(totalSizePanel)

        val btnShowAllFiles = JLabel("全部文件")
        btnShowAllFiles.isOpaque = true
        btnShowAllFiles.horizontalAlignment = SwingConstants.CENTER
        btnShowAllFiles.preferredSize = Dimension(150, 50)
        menuView.add(btnShowAllFiles)

        val spaceLinePanel = JPanel()
        spaceLinePanel.preferredSize = Dimension(150, 1)
        menuView.add(spaceLinePanel)

        val btnShowDownloadFiles = JLabel("正在下载")
        btnShowDownloadFiles.isOpaque = true
        btnShowDownloadFiles.horizontalAlignment = SwingConstants.CENTER
        btnShowDownloadFiles.preferredSize = Dimension(150, 50)
        menuView.add(btnShowDownloadFiles)

        btnShowAllFiles.background = Color(225, 225, 225)
        btnShowDownloadFiles.background = bgMenu

        btnShowAllFiles.onClickListener {
            btnShowAllFiles.background = Color(225, 225, 225)
            btnShowDownloadFiles.background = bgMenu

            cardLayout.show(contentPanel, "listFilesPanel")
            refreshDownloadProgressDisposable?.apply {
                if (!isDisposed) {
                    dispose()
                }
            }
        }
        btnShowDownloadFiles.onClickListener {
            btnShowAllFiles.background = bgMenu
            btnShowDownloadFiles.background = Color(225, 225, 225)

            cardLayout.show(contentPanel, "listDownloadPanel")
            reloadListDownload()
            refreshDownloadProgressDisposable?.apply {
                if (!isDisposed) {
                    dispose()
                }
            }
            refreshDownloadProgressDisposable = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribe { _ ->
                        reloadListDownload()
                    }
        }

        add(menuView, BorderLayout.WEST)
    }

    fun downloadFile(vararg files: BaiduFile) {
        // 下载文件or文件夹
        Downloader.putDownloadTask(*files)
                .subscribe({
                    Toast.show("添加下载任务成功")
                }, {
                    LogUtils.e(it)
                    Toast.show(it)
                })
    }

    fun deleteFile(vararg files: BaiduFile) {
        // 删除文件or文件夹
        BaiduRepository.deleteFiles(*files.map { it.path }.toTypedArray())
                .subscribe({
                    reloadData()
                    Toast.show("删除成功")
                }, {
                    Toast.show(it)
                })
    }

    fun createNewDir() {
        AlertDialogManager.showInputDialog(this, "请输入文件夹名") { input ->
            val path = input.trim().removePrefix("/").removeSuffix("/")

            if (path.isEmpty()) {
                Toast.show("文件夹名不能为空！")
                return@showInputDialog
            }

            val specialSymbolArr = arrayOf("\"", ":", "\\", "/", "|", "*")
            specialSymbolArr.forEach {
                if (path.contains(it)) {
                    Toast.show("不能包含特殊符号[ $it ]！")
                    return@showInputDialog
                }
            }

            BaiduRepository.createNewDir(currentDir.removeSuffix("/") + "/" + path)
                    .subscribe({
                        Toast.show("创建成功")
                        reloadData()
                    }, {
                        Toast.show("创建失败，请重试")
                    })
        }
    }


    override fun setVisible(isVisible: Boolean) {
        super.setVisible(isVisible)
        if (isVisible) {
            refreshNav()
            reloadData()
        }
    }

    fun changeBtnsVisible() {
        val selectedFiles = listFilesView.getSelectedData()
        val hasSelectedData = selectedFiles.isNotEmpty()
        btnDownload.isVisible = hasSelectedData
        btnDelete.isVisible = hasSelectedData
    }

    fun changeCurrentDir(dir: String) {
        if (currentDir.equals(dir)) {
            return
        }
        currentDir = dir
        refreshNav()
        reloadData()
    }

    fun reloadData() {
        BaiduRepository.listFiles(currentDir)
                .subscribe({
                    listFilesView.adapter?.clear()
                    listFilesView.adapter?.addAll(it)
                    listFilesView.adapter?.notifyDataChanged()
                    changeBtnsVisible()
                }, {
                    Toast.show(it.description)
                })
    }

    fun refreshNav() {
        navigationView.removeAll()
        val hierarchy = BaiduUtils.parseDirHierarchy(currentDir)
        hierarchy.forEach { dir ->
            val label = JLabel()
            var name = dir
            if (dir.isEmpty()) {
                name = "我的网盘"
            } else if (dir.contains("/")) {
                name = dir.substring(dir.lastIndexOf("/")+1)
                if ("apps" == name) {
                    name = "我的应用数据"
                }
            }
            label.text = "$name >"
            label.border = EmptyBorder(0, 8, 0, 0)
            label.onSingleClickListener {
                changeCurrentDir(if (dir.isEmpty()) "/" else dir)
            }
            label.onDoubleClickListener {
                changeCurrentDir(if (dir.isEmpty()) "/" else dir)
            }
            navigationView.add(label)
        }
        navigationView.updateUI()
    }

    private val progressFormat = DecimalFormat("0.##")
    private var lastTime = System.currentTimeMillis()
    private var lastDownloadCount = 0L

    fun reloadListDownload() {
        Downloader.listDownloadProject()
                .subscribe({ allProgress ->
                    listDownloadView.adapter?.clear()
                    listDownloadView.adapter?.addAll(allProgress)
                    listDownloadView.adapter?.notifyDataChanged()

                    val downloadCount = allProgress.sumByLong { it.downloadSize }
                    val totalCount = allProgress.sumByLong { it.totalSize }
                    SwingUtilities.invokeLater {
                        if (totalCount == 0L) {
                            progress.value = 1000
                            progress.updateUI()
                            labelNetspeed.text = "100%"
                        } else {
                            progress.value = (1000 * downloadCount / totalCount).toInt()
                            progress.updateUI()

                            val duration = System.currentTimeMillis() - lastTime
                            val percent = "${progressFormat.format((10000 * downloadCount / totalCount).toInt() * 0.01)}%"
                            if (lastDownloadCount == 0L && duration > 0 && downloadCount - lastDownloadCount > 0) {
                                labelNetspeed.text = percent
                            } else {
                                val speed = "${FileUtils.formatFileSize((downloadCount - lastDownloadCount) * 1000 / duration)}/s"
                                labelNetspeed.text = "$percent  $speed"
                            }
                            lastTime += duration
                            lastDownloadCount = downloadCount
                        }
                    }
                }, {
                    LogUtils.e(it)
                    Toast.show(it.description)
                })
    }

}
