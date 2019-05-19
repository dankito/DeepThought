package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.model.Item


class EditItemView : EditItemViewBase() {

    private lateinit var item: Item


    override val windowDataClass = Item::class.java


    init {
        (windowData as? Item)?.let { item ->
            setItem(item)
        }
    }

    private fun setItem(item: Item) {
        this.item = item

        showData(item, item.tags, item.source, item.source?.series, item.attachedFiles)

        hasUnsavedChanges.value = false
    }

    override fun getCurrentWindowData() = item

}