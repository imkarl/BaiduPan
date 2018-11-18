package cn.imkarl.baidupan.ui.widget.list

import cn.imkarl.baidupan.ui.widget.setOnClickListener
import java.awt.event.MouseEvent
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.JScrollPane

/**
 * 列表控件
 * @author imkarl
 */
open class ListView<DATA>: JScrollPane(JList<DATA>()) {

    private val defaultListModel by lazy {
        object : AbstractListModel<DATA>() {
            override fun getSize(): Int {
                return 0
            }
            override fun getElementAt(var1: Int): DATA {
                throw IndexOutOfBoundsException("No Data Model")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val list by lazy { super.viewport.view as JList<DATA> }

    var adapter: ListAdapter<DATA>?
        get() { return if (list.model is ListAdapter<DATA>) list.model as ListAdapter<DATA> else null }
        set(value) {
            if (value != null) {
                list.cellRenderer = value
                list.model = value
            } else {
                list.cellRenderer = null
                list.model = defaultListModel
            }
        }

    var itemHeight: Int
        get() { return list.fixedCellHeight }
        set(value) { list.fixedCellHeight = value }

    fun setOnClickListener(singleClick: (MouseEvent)->Unit, doubleClick: ((MouseEvent)->Unit)? = null) {
        list.setOnClickListener(singleClick, doubleClick)
    }

}