package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Item


class EditEntryView : EditEntryViewBase() {

    val item: Item by param()


    init {
        showData(item, item.tags, item.source, item.source?.series, item.attachedFiles)

        hasUnsavedChanges.value = false
    }

}