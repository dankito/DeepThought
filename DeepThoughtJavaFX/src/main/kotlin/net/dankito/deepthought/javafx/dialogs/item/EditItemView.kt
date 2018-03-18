package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.model.Item


class EditItemView : EditItemViewBase() {

    val item: Item by param()


    init {
        showData(item, item.tags, item.source, item.source?.series, item.attachedFiles)

        hasUnsavedChanges.value = false
    }

}