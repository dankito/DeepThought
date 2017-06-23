package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class ReadLaterArticlePresenter(private val view: IReadLaterArticleView, private val readLaterArticleService: ReadLaterArticleService, private val entryPersister: EntryPersister,
                                private val router: IRouter) {


    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var threadPool: IThreadPool


    private val eventBusListener = EventBusListener()


    init {
        CommonComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    fun getReadLaterArticlesAsync() {
        threadPool.runAsync { getReadLaterArticles() }
    }

    private fun getReadLaterArticles() {
        readLaterArticleService.dataManager.currentDeepThought?.readLaterArticles?.let {
            readLaterArticlesRetrieved(it)
            return
        }

        readLaterArticleService.getAllAsync {
            readLaterArticlesRetrieved(it)
        }
    }

    private fun readLaterArticlesRetrieved(articles: List<ReadLaterArticle>) {
        val extractionResultToArticlesToMap = LinkedHashMap<EntryExtractionResult, ReadLaterArticle>()

        articles.forEach { extractionResultToArticlesToMap.put(mapReadLaterArticleToEntryExtractionResult(it), it) }

        view.showArticles(extractionResultToArticlesToMap)
    }

    private fun mapReadLaterArticleToEntryExtractionResult(readLaterArticle: ReadLaterArticle): EntryExtractionResult {
        return serializer.deserializeObject(readLaterArticle.serializedEntryExtractionResult, EntryExtractionResult::class.java)
    }


    fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
    }

    fun saveAndDeleteReadLaterArticle(extractionResult: EntryExtractionResult, article: ReadLaterArticle?) {
        saveReadLaterArticle(extractionResult)

        deleteReadLaterArticle(article)
    }

    fun saveReadLaterArticle(extractionResult: EntryExtractionResult) = entryPersister.saveEntry(extractionResult)

    fun deleteReadLaterArticle(article: ReadLaterArticle?) {
        article?.let {
            readLaterArticleService.delete(it)
        }
    }

    fun  copyUrlToClipboard(extractionResult: EntryExtractionResult) {
        // TODO
    }


    inner class EventBusListener {

        @Handler
        fun readLaterArticleChanged(readLaterArticleChanged: ReadLaterArticleChanged) {
            getReadLaterArticlesAsync()
        }

    }

}