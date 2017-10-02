package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult


class EditEntryExtractionResultView : EditEntryViewBase() {

    val extractionResult: EntryExtractionResult by param()


    init {
        showData(extractionResult.entry, extractionResult.tags, extractionResult.reference, extractionResult.series)
    }


    override fun getEntryForSaving(): Entry {
        return extractionResult.entry
    }

    override fun getReferenceForSaving(): Reference? {
        return extractionResult.reference
    }

    override fun getCurrentSeries(): Series? {
        return extractionResult.series
    }

    override fun getTagsForSaving(): List<Tag> {
        return extractionResult.tags
    }


}