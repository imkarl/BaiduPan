package cn.imkarl.baidupan.ui.widget

import io.reactivex.subjects.PublishSubject
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 设置点击事件监听
 */
fun Component.setOnClickListener(singleClick: ((MouseEvent)->Unit)?, doubleClick: ((MouseEvent)->Unit)? = null) {
    this.addMouseListener(object: MouseAdapter() {
        private var publish = PublishSubject.create<Pair<Int, MouseEvent>>()
        private val codeGenerator = AtomicInteger(1)
        private var isDoubleClick = false
        private var lastRequestCode = -1
        init {
            publish.throttleLast(650, TimeUnit.MILLISECONDS)
                    .subscribe { pair ->
                        if (!isDoubleClick && pair.first == lastRequestCode) {
                            singleClick?.invoke(pair.second)
                        }
                    }
        }
        override fun mouseClicked(event: MouseEvent) {
            if (event.clickCount % 2 == 1) {
                // 单击
                isDoubleClick = false
                lastRequestCode = codeGenerator.getAndIncrement()
                publish.onNext(Pair(lastRequestCode, event))
            } else if (event.clickCount % 2 == 0) {
                // 双击
                isDoubleClick = true
                doubleClick?.invoke(event)
                event.consume()
            }
        }
    })
}

fun Component.onSingleClickListener(singleClick: (MouseEvent) -> Unit) {
    setOnClickListener(singleClick, null)
}

fun Component.onDoubleClickListener(doubleClick: (MouseEvent) -> Unit) {
    setOnClickListener(null, doubleClick)
}

fun Component.onClickListener(click: (MouseEvent) -> Unit) {
    setOnClickListener(click, click)
}

fun Component.callOnClick() {
    val mouseEvent = MouseEvent(this, 0, 0, 0, 0, 0, 0, false)
    this.mouseListeners?.forEach {
        it.mouseClicked(mouseEvent)
    }
}
