package net.dankito.deepthought.javafx.dialogs.mainwindow

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.EntryViewModel
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.news.summary.config.ConfigChangedListener
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.EntryService
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


    val mainWindow: MainWindow by inject()


    fun init() {
        AppComponent.component.inject(this)

        extractorsConfigManager.addListener(articleSummaryExtractorConfigChangedListener)
        extractorsConfigManager.getConfigs().forEach { mainWindow.addArticleSummaryExtractor(it) }
    }


    private val articleSummaryExtractorConfigChangedListener = object : ConfigChangedListener {
        override fun configChanged(config: ArticleSummaryExtractorConfig) {
            FXUtils.runOnUiThread { mainWindow.articleSummaryExtractorUpdated(config) }
        }
    }

    fun showArticlesSummaryView(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        router.showArticleSummaryView(articleSummaryExtractorConfig)
    }

    fun showReadLaterArticlesView() {
        router.showReadLaterArticlesView()
    }

}