package com.chimerapps.storageinspector.ui.util.list

import com.chimerapps.storageinspector.ui.util.dispatchMain
import com.intellij.util.ui.ListTableModel
import kotlin.concurrent.thread

interface DiffUtilDispatchModel<T> : ListUpdateCallback {

    fun setItems(items: List<T>)
}

interface DiffUtilComparator<T> {
    fun representSameItem(left: T, right: T): Boolean
    fun areItemContentsEqual(left: T, right: T): Boolean = left == right
}

class ListUpdateHelper<T>(
    private val model: DiffUtilDispatchModel<T>,
    private val comparator: DiffUtilComparator<T>,
) {

    private var hasInit = false
    private val internalListData = mutableListOf<T>()
    private var oldRunThread: Thread? = null

    fun onListUpdated(
        newListData: List<T>,
    ) {
        synchronized(internalListData) {
            if (!hasInit) {
                hasInit = true
                internalListData.addAll(newListData)
                model.setItems(ArrayList(internalListData))
                return
            }
        }
        oldRunThread?.interrupt()
        oldRunThread?.join()

        oldRunThread = thread(name = "DiffUtil") {
            val oldListCopy = synchronized(internalListData) { ArrayList(internalListData) }
            try {
                val thread = Thread.currentThread()
                val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override val oldListSize: Int =
                        if (thread.isInterrupted) throw InterruptedException() else oldListCopy.size
                    override val newListSize: Int =
                        if (thread.isInterrupted) throw InterruptedException() else newListData.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        if (thread.isInterrupted) throw InterruptedException()
                        return comparator.representSameItem(
                            oldListCopy[oldItemPosition],
                            newListData[newItemPosition]
                        )
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        if (thread.isInterrupted) throw InterruptedException()
                        return comparator.areItemContentsEqual(
                            oldListCopy[oldItemPosition],
                            newListData[newItemPosition]
                        )
                    }
                })
                dispatchMain {
                    if (!thread.isInterrupted) {
                        synchronized(internalListData) {
                            internalListData.clear()
                            internalListData.addAll(newListData)
                            model.setItems(ArrayList(newListData))
                        }

                        diff.dispatchUpdatesTo(model)
                    }
                }
            } catch (e: Throwable) {
                //Ignore
            }
        }
    }
}

@Suppress("unused")
class TableModelDiffUtilDispatchModel<T>(private val model: ListTableModel<T>) : DiffUtilDispatchModel<T> {
    override fun onInserted(position: Int, count: Int) {
        model.fireTableRowsInserted(position, position + count)
    }

    override fun onRemoved(position: Int, count: Int) {
        model.fireTableRowsDeleted(position, position + count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        model.fireTableRowsUpdated(fromPosition, fromPosition)
        model.fireTableRowsUpdated(toPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        model.fireTableRowsUpdated(position, position + count)
    }

    override fun setItems(items: List<T>) {
        model.items = items
    }
}