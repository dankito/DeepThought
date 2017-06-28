package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewEntryActivity
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer


class AndroidRouter(private val context: Context, private val activityTracker: CurrentActivityTracker, private val serializer: ISerializer) : IRouter {


    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        // TODO: what to do on Android?
    }


    override fun showArticleSummaryExtractorsView() {
        activityTracker.currentActivity?.let { currentActivity ->
            val articleSummaryExtractorsDialog = ArticleSummaryExtractorsDialog(currentActivity)
            articleSummaryExtractorsDialog.showDialog()
        }
    }

    override fun showAddArticleSummaryExtractorView() {
        activityTracker.currentActivity?.supportFragmentManager?.let { fragmentManager ->
            val addArticleSummaryExtractorDialog = AddArticleSummaryExtractorDialog()

            addArticleSummaryExtractorDialog.show(fragmentManager, AddArticleSummaryExtractorDialog.TAG)
        }
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        val articleSummaryActivityIntent = Intent(context, ArticleSummaryActivity::class.java)
        articleSummaryActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        articleSummaryActivityIntent.putExtra(ArticleSummaryActivity.EXTRACTOR_URL_INTENT_EXTRA_NAME, extractor.url)

        context.startActivity(articleSummaryActivityIntent)
    }

    override fun showReadLaterArticlesView() {
        // TODO
    }


    override fun showViewEntryView(entry: Entry) {
        entry.id?.let { entryId -> showEntryView(ViewEntryActivity.ENTRY_ID_INTENT_EXTRA_NAME, entryId) }
    }

    override fun showViewEntryView(extractionResult: EntryExtractionResult) {
        val serializedExtractionResult = serializer.serializeObject(extractionResult)

        showEntryView(ViewEntryActivity.ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializedExtractionResult)
    }

    private fun showEntryView(intentExtraName: String, intentExtraValue: String) {
        val viewArticleIntent = Intent(context, ViewEntryActivity::class.java)
        viewArticleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        viewArticleIntent.putExtra(intentExtraName, intentExtraValue)

        context.startActivity(viewArticleIntent)
    }


    override fun showEditEntryView(entry: Entry) {

    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult) {

    }


    override fun returnToPreviousView() {
        activityTracker.currentActivity?.onBackPressed()
    }

}