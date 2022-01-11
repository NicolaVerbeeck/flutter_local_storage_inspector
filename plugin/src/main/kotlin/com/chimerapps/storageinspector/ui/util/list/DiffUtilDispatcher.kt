package com.chimerapps.storageinspector.ui.util.list

import com.chimerapps.storageinspector.ui.util.ensureMain
import com.intellij.util.ui.ListTableModel

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

    fun onListUpdated(
        newListData: List<T>,
    ) {
        ensureMain {
            doListUpdate(newListData)
        }
    }

    private fun doListUpdate(newListData: List<T>) {
        if (!hasInit) {
            hasInit = true
            internalListData.addAll(newListData)
            model.setItems(ArrayList(internalListData))
            return
        }

        val newListCopy = ArrayList(newListData)

        try {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override val oldListSize: Int = internalListData.size
                override val newListSize: Int = newListCopy.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return comparator.representSameItem(
                        internalListData[oldItemPosition],
                        newListCopy[newItemPosition]
                    )
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return comparator.areItemContentsEqual(
                        internalListData[oldItemPosition],
                        newListCopy[newItemPosition]
                    )
                }
            })
            internalListData.clear()
            internalListData.addAll(newListCopy)
            model.setItems(newListCopy)

            diff.dispatchUpdatesTo(model)
        } catch (e: Throwable) {
            //Ignore
        }
    }
}

class TableModelDiffUtilDispatchModel<T>(private val model: ListTableModel<T>) : DiffUtilDispatchModel<T> {
    override fun onInserted(position: Int, count: Int) {
        model.fireTableRowsInserted(position, position + count - 1)
    }

    override fun onRemoved(position: Int, count: Int) {
        model.fireTableRowsDeleted(position, position + count - 1)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        model.fireTableRowsUpdated(fromPosition, fromPosition)
        model.fireTableRowsUpdated(toPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        model.fireTableRowsUpdated(position, position + count - 1)
    }

    override fun setItems(items: List<T>) {
        model.items = items
    }
}