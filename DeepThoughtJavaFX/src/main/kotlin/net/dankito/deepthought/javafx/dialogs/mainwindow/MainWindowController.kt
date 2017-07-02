package net.dankito.deepthought.javafx.dialogs.mainwindow

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.EntryViewModel
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.EntryService
import net.dankito.service.data.messages.ArticleSummaryExtractorConfigChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler
import tornadofx.*
import javax.inject.Inject


class MainWindowController : Controller() {


    val entryModel = EntryViewModel()

    val entries = LazyLoadingObservableList<Entry>()


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    private val eventBusListener = EventBusListener()


    val mainWindow: MainWindow by inject()


    fun init() {
        AppComponent.component.inject(this)

        extractorsConfigManager.addInitializationListener { runLater {
            extractorsConfigManager.getConfigs().forEach { mainWindow.addArticleSummaryExtractor(it) }
        } }

        eventBus.register(eventBusListener)
    }


    fun showArticlesSummaryView(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        router.showArticleSummaryView(articleSummaryExtractorConfig)
    }

    fun showReadLaterArticlesView() {
        router.showReadLaterArticlesView()
    }


    inner class EventBusListener {

        @Handler
        fun articleSummaryExtractorChanged(changed: ArticleSummaryExtractorConfigChanged) {
            FXUtils.runOnUiThread { mainWindow.articleSummaryExtractorUpdated(changed.entity) }
        }
    }

}