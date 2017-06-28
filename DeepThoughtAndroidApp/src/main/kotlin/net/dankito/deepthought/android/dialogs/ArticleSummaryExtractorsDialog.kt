package net.dankito.deepthought.android.dialogs

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.news.summary.config.ConfigChangedListener
import net.dankito.deepthought.ui.IRouter
import javax.inject.Inject


class ArticleSummaryExtractorsDialog(private val activity: AppCompatActivity) {

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var summaryExtractorsManager: ArticleSummaryExtractorConfigManager

    private var adapter: ArticleSummaryExtractorsAdapter


    init {
        AppComponent.component.inject(this)

        adapter = ArticleSummaryExtractorsAdapter(activity, summaryExtractorsManager)
    }


    fun showDialog() {
        var builder = AlertDialog.Builder(activity)
        builder = builder.setAdapter(adapter, { dialog, which ->
            val selectedExtractor = adapter.getItem(which)
            showSelectedSummaryExtractor(selectedExtractor)
            dialog.dismiss()
        })

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })

        builder.setNeutralButton(R.string.dialog_article_summary_extractors_add_extractor, { dialog, _ ->
            showAddArticleSummaryExtractorView()
            dialog.dismiss()
        })

        builder.setOnDismissListener { summaryExtractorsManager.removeListener(articleSummaryExtractorConfigChangedListener)  }

        builder.create().show()

        summaryExtractorsManager.addListener(articleSummaryExtractorConfigChangedListener)
    }

    private val articleSummaryExtractorConfigChangedListener = object : ConfigChangedListener {
        override fun configChanged(config: ArticleSummaryExtractorConfig) {
            activity.runOnUiThread { adapter.notifyDataSetChanged() }
        }
    }


    private fun showSelectedSummaryExtractor(selectedExtractor: ArticleSummaryExtractorConfig) {
        router.showArticleSummaryView(selectedExtractor)
    }

    private fun showAddArticleSummaryExtractorView() {
        router.showAddArticleSummaryExtractorView()
    }

}