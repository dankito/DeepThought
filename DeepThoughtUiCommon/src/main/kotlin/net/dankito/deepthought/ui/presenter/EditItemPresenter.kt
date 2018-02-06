package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EditItemPresenter(private val itemPersister: ItemPersister, private val readLaterArticleService: ReadLaterArticleService, private val clipboardService: IClipboardService,
                        private val router: IRouter) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun saveItemAsync(item: Item, source: Source?, series: Series?, tags: Collection<Tag>, files: Collection<FileLink>, callback: (Boolean) -> Unit) {
        itemPersister.saveItemAsync(item, source, series, tags, files, callback)
    }

    fun saveItemExtractionResultForLaterReading(extractionResult: ItemExtractionResult) {
        readLaterArticleService.persist(ReadLaterArticle(extractionResult, "",
                extractionResult.source.preview, extractionResult.source?.url, extractionResult.source?.previewImageUrl)) // articlePreview gets set in ReadLaterArticleService
    }

    fun deleteReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.delete(article)
    }


    fun shareSourceUrl(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun shareItem(item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        clipboardService.copyItemToClipboard(item, tags, source, series)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}