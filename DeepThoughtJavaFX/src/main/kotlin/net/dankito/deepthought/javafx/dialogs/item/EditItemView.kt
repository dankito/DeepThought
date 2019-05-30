package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.windowdata.EditItemWindowData


class EditItemView : EditItemViewBase() {

    private lateinit var editItemWindowData: EditItemWindowData


    override val windowDataClass = EditItemWindowData::class.java


    init {
        (windowData as? EditItemWindowData)?.let { editItemWindowData ->
            this.editItemWindowData = editItemWindowData

            setItem(editItemWindowData.item)
            restoreWindowData(editItemWindowData)
        }
    }

    private fun setItem(item: Item) {
        showData(item, item.tags, item.source, item.source?.series, item.attachedFiles)

        hasUnsavedChanges.value = false
    }

    override fun getCurrentWindowData(): Any? {
        updateWindowData(editItemWindowData)

        return editItemWindowData
    }

}