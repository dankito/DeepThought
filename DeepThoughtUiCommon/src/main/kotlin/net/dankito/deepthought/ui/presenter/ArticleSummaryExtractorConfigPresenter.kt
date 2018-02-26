package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.util.IThreadPool
import javax.inject.Inject


class ArticleSummaryExtractorConfigPresenter(private val extractorsConfigManager: ArticleSummaryExtractorConfigManager) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun saveAsync(config: ArticleSummaryExtractorConfig, callback: (Boolean) -> Unit) {
        if(config.isPersisted()) {
            updateConfigAsync(config, callback)
        }
        else {
            feedAdded(config, callback)
        }
    }

    private fun feedAdded(config: ArticleSummaryExtractorConfig, callback: (Boolean) -> Unit) {
        extractorsConfigManager.addFeed(config.url, config) {
            callback(true)
        }
    }

    private fun updateConfigAsync(config: ArticleSummaryExtractorConfig, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            extractorsConfigManager.configurationUpdated(config)
            callback(true)
        }
    }


    fun deleteConfigAsync(config: ArticleSummaryExtractorConfig, callback: () -> Unit) {
        threadPool.runAsync {
            extractorsConfigManager.deleteConfig(config)

            callback()
        }
    }

}