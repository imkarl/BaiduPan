package cn.imkarl.baidupan.ui.widget.list

import java.awt.Component
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.ListCellRenderer

/**
 * 列表适配器
 * @author imkarl
 */
class ListAdapter<DATA>(createItemView: ()-> ListItemView<DATA>)
    : AbstractListModel<DATA>(), ListCellRenderer<DATA> {

    private val items by lazy { mutableListOf<DATA>() }
    private val itemView by lazy { createItemView.invoke() }

    override fun getListCellRendererComponent(
            list: JList<out DATA>?,
            value: DATA,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean): Component {
        itemView.bindData(value, index, isSelected, cellHasFocus)
        return itemView
    }

    fun notifyDataChanged() {
        fireContentsChanged(this, 0, size)
    }

    override fun getSize(): Int {
        return items.size
    }

    override fun getElementAt(index: Int): DATA {
        return items[index]
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
