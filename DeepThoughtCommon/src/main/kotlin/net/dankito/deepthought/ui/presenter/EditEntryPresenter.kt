package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getEntryPreview
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
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


    fun editReference(source: Source?, forItem: Item, series: Series?) {
        router.showEditEntryReferenceView(forItem, source, series)
    }

    fun saveEntryAsync(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        entryPersister.saveEntryAsync(item, source, series, tags, callback)
    }

    fun saveEntryExtractionResultForLaterReading(extractionResult: ItemExtractionResult) {
        extractionResult.item.preview = extractionResult.item.getEntryPreview(true)
        val articlePreview = extractionResult.item.getEntryPreviewWithSeriesAndPublishingDate(extractionResult.source, extractionResult.series)

        readLaterArticleService.persist(ReadLaterArticle(extractionResult, articlePreview,
                extractionResult.source.preview, extractionResult.source?.url, extractionResult.source?.previewImageUrl))
    }

    fun deleteReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.delete(article)
    }


    fun shareReferenceUrl(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun shareEntry(item: Item, source: Source?, series: Series?) {
        clipboardService.copyEntryToClipboard(item, source, series)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}