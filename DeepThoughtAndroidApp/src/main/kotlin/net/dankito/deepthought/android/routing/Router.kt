package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewEntryActivity
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer


class Router(private val context: Context, private val serializer: ISerializer) {


    fun showAddArticleSummaryExtractorView(fragmentManager: FragmentManager) {
        val addArticleSummaryExtractorDialog = AddArticleSummaryExtractorDialog()

        addArticleSummaryExtractorDialog.show(fragmentManager, AddArticleSummaryExtractorDialog.TAG)
    }

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        val articleSummaryActivityIntent = Intent(context, ArticleSummaryActivity::class.java)

        articleSummaryActivityIntent.putExtra(ArticleSummaryActivity.EXTRACTOR_URL_INTENT_EXTRA_NAME, extractor.url)

        context.startActivity(articleSummaryActivityIntent)
    }

    fun showEntryView(entry: Entry) {
        val serializedEntry = serializer.serializeObject(entry)

        showEntryView(ViewEntryActivity.ENTRY_INTENT_EXTRA_NAME, serializedEntry)
    }

    fun showEntryView(extractionResult: EntryExtractionResult) {
        val serializedExtractionResult = serializer.serializeObject(extractionResult)

        showEntryView(ViewEntryActivity.ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializedExtractionResult)
    }

    private fun showEntryView(intentExtraName: String, intentExtraValue: String) {
        val viewArticleIntent = Intent(context, ViewEntryActivity::class.java)

        viewArticleIntent.putExtra(intentExtraName, intentExtraValue)

        context.startActivity(viewArticleIntent)
    }

}