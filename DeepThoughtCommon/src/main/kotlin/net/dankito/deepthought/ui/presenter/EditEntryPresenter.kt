package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.data.EntryPersister
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EditEntryPresenter(private val entryPersister: EntryPersister, private val readLaterArticleService: ReadLaterArticleService, private val clipboardService: IClipboardService,
                         private val router: IRouter) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun editReference(reference: Reference?, forEntry: Entry, series: Series?) {
        router.showEditEntryReferenceView(forEntry, reference, series)
    }

    fun saveEntryAsync(entry: Entry, reference: Reference? = null, series: Series? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        entryPersister.saveEntryAsync(entry, reference, series, tags, callback)
    }

    fun saveEntryExtractionResultForLaterReading(extractionResult: EntryExtractionResult) {
        readLaterArticleService.persist(ReadLaterArticle(extractionResult, extractionResult.entry.getEntryPreviewWithSeriesAndPublishingDate(extractionResult.reference, extractionResult.series),
                extractionResult.reference.preview, extractionResult.reference?.url, extractionResult.reference?.previewImageUrl))
    }

    fun deleteReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.delete(article)
    }


    fun shareReferenceUrl(reference: Reference) {
        reference.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun shareEntry(entry: Entry, reference: Reference?, series: Series?) {
        clipboardService.copyEntryToClipboard(entry, reference, series)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}