package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.util.EntryExtractionResult


class EditEntryExtractionResultView : EditEntryViewBase() {

    val extractionResult: EntryExtractionResult by param()


    init {
        showData(extractionResult.entry, extractionResult.tags, extractionResult.reference, extractionResult.series)
    }


}