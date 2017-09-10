package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
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


    fun editReference(reference: Reference?, forEntry: Entry) {
        router.showEditEntryReferenceView(forEntry, reference)
    }

    fun saveEntryAsync(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        entryPersister.saveEntryAsync(entry, reference, tags, callback)
    }

    fun saveEntryExtractionResultForLaterReading(entryExtractionResult: EntryExtractionResult) {
        readLaterArticleService.persist(ReadLaterArticle(entryExtractionResult))
    }

    fun deleteReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.delete(article)
    }


    fun shareReferenceUrl(reference: Reference) {
        reference.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun shareEntry(entry: Entry, reference: Reference?) {
        clipboardService.copyEntryToClipboard(entry, reference)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}