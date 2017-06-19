package net.dankito.deepthought.javafx.dialogs.mainwindow

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.news.summary.config.ConfigChangedListener
import tornadofx.*
import javax.inject.Inject


class MainWindowController : Controller() {


    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager


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

}