package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.EditEntryActivity
import net.dankito.deepthought.android.activities.ViewEntryActivity
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.ReferenceEntriesListDialog
import net.dankito.deepthought.android.dialogs.TagEntriesListDialog
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.serialization.ISerializer


class AndroidRouter(private val context: Context, private val activityTracker: CurrentActivityTracker, private val serializer: ISerializer) : IRouter {


    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = TagEntriesListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, tag)
        }
    }

    override fun showEntriesForReference(reference: Reference) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = ReferenceEntriesListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, reference)
        }
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
        // is a tab on main screen in Android, no need for routing
    }


    override fun showViewEntryView(entry: Entry) {
        entry.id?.let { entryId -> showViewEntryView(ViewEntryActivity.ENTRY_ID_INTENT_EXTRA_NAME, entryId) }
    }

    override fun showViewEntryView(article: ReadLaterArticle) {
        article.id?.let { articleId -> showViewEntryView(ViewEntryActivity.READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, articleId) }
    }

    override fun showViewEntryView(extractionResult: EntryExtractionResult) {
        val serializedExtractionResult = serializer.serializeObject(extractionResult)

        showViewEntryView(ViewEntryActivity.ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializedExtractionResult)
    }

    private fun showViewEntryView(intentExtraName: String, intentExtraValue: String) {
        val viewArticleIntent = Intent(context, ViewEntryActivity::class.java)
        viewArticleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        viewArticleIntent.putExtra(intentExtraName, intentExtraValue)

        context.startActivity(viewArticleIntent)
    }


    override fun showCreateEntryView() {
        showEditEntryView()
    }

    override fun showEditEntryView(entry: Entry) {
        showEditEntryView(EditEntryActivity.ENTRY_ID_INTENT_EXTRA_NAME, entry.id)
    }

    override fun showEditEntryView(article: ReadLaterArticle) {
        showEditEntryView(EditEntryActivity.READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, article.id)
    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult) {
        val serializedExtractionResult = serializer.serializeObject(extractionResult)

        showEditEntryView(EditEntryActivity.ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializedExtractionResult)
    }

    private fun showEditEntryView(intentExtraName: String? = null, intentExtraValue: String? = null) {
        val editEntryIntent = Intent(context, EditEntryActivity::class.java)
        editEntryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        intentExtraName?.let { editEntryIntent.putExtra(intentExtraName, intentExtraValue) }

        context.startActivity(editEntryIntent)
    }


    override fun returnToPreviousView() {
        activityTracker.currentActivity?.onBackPressed()
    }

}