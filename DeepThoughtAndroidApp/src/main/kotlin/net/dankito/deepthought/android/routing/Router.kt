package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewArticleActivity
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
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

    fun showArticleView(extractionResult: EntryExtractionResult) {
        val serializedArticle = serializer.serializeObject(extractionResult)

        val viewArticleIntent = Intent(context, ViewArticleActivity::class.java)

        viewArticleIntent.putExtra(ViewArticleActivity.ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializedArticle)

        context.startActivity(viewArticleIntent)
    }

}