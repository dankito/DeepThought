package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Entry


class EditEntryView : EditEntryViewBase() {

    val entry: Entry by param()


    init {
        showData(entry, entry.tags, entry.reference, entry.reference?.series)

        hasUnsavedChanges.value = false
    }

}