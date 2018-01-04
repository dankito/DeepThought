package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
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


    fun saveEntryAsync(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        entryPersister.saveEntryAsync(item, source, series, tags, callback)
    }

    fun saveEntryExtractionResultForLaterReading(extractionResult: ItemExtractionResult) {
        readLaterArticleService.persist(ReadLaterArticle(extractionResult, "",
                extractionResult.source.preview, extractionResult.source?.url, extractionResult.source?.previewImageUrl)) // articlePreview gets set in ReadLaterArticleService
    }

    fun deleteReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.delete(article)
    }


    fun shareReferenceUrl(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun shareEntry(item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        clipboardService.copyEntryToClipboard(item, tags, source, series)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}