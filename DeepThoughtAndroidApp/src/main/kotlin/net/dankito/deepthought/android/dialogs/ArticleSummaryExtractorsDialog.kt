package net.dankito.deepthought.android.dialogs

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.routing.Router
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.news.summary.config.ConfigChangedListener


class ArticleSummaryExtractorsDialog(private val activity: AppCompatActivity) {

    private val router = Router(activity.applicationContext) // TODO: inject

    // TODO: inject
    private val summaryExtractors = ArticleSummaryExtractorConfigManager(OkHttpWebClient(), AndroidFileStorageService(activity))

    private val adapter = ArticleSummaryExtractorsAdapter(activity, summaryExtractors.getConfigs())


    fun showDialog() {
        var builder = AlertDialog.Builder(activity)
        builder = builder.setAdapter(adapter, { dialog, which ->
            val selectedExtractor = adapter.getItem(which)
            showSelectedSummaryExtractor(selectedExtractor)
            dialog.cancel()
        })

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })

        builder.setOnDismissListener { summaryExtractors.removeListener(articleSummaryExtractorConfigChangedListener)  }

        builder.create().show()

        summaryExtractors.addListener(articleSummaryExtractorConfigChangedListener)
    }

    private val articleSummaryExtractorConfigChangedListener = object : ConfigChangedListener {
        override fun configChanged(config: ArticleSummaryExtractorConfig) {
            activity.runOnUiThread { adapter.notifyDataSetChanged() }
        }
    }


    private fun showSelectedSummaryExtractor(selectedExtractor: ArticleSummaryExtractorConfig) {
        router.showArticleSummaryView(selectedExtractor)
    }

}