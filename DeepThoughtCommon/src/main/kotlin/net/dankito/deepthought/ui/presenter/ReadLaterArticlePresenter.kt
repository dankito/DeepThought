package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.serializer.ISerializer
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.Search
import net.dankito.utils.IThreadPool
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class ReadLaterArticlePresenter(private val view: IReadLaterArticleView, private val readLaterArticleService: ReadLaterArticleService, private val entryPersister: EntryPersister,
                                private val router: IRouter) : IMainViewSectionPresenter {


    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var threadPool: IThreadPool


    private var lastSearchTermProperty = Search.EmptySearchTerm

    private val eventBusListener = EventBusListener()


    init {
        CommonComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }

    override fun getAndShowAllEntities() {
        threadPool.runAsync { getReadLaterArticles() }
    }

    private fun getReadLaterArticles() {
        readLaterArticleService.getAllAsync {
            view.showArticles(it)
        }
    }


    fun showArticle(article: ReadLaterArticle) {
        router.showViewEntryView(article.entryExtractionResult)
    }

    fun saveAndDeleteReadLaterArticle(article: ReadLaterArticle) {
        saveReadLaterArticle(article)

        deleteReadLaterArticle(article)
    }

    fun saveReadLaterArticle(article: ReadLaterArticle) = entryPersister.saveEntry(article.entryExtractionResult)

    fun deleteReadLaterArticle(article: ReadLaterArticle?) {
        article?.let {
            readLaterArticleService.delete(it)
        }
    }

    fun  copyUrlToClipboard(article: ReadLaterArticle) {
        // TODO
    }


    inner class EventBusListener {

        @Handler
        fun readLaterArticleChanged(readLaterArticleChanged: ReadLaterArticleChanged) {
            getAndShowAllEntities()
        }

    }

}