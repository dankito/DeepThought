package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.activities.arguments.EntryActivityParameters
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.ReferenceEntriesListDialog
import net.dankito.deepthought.android.dialogs.TagEntriesListDialog
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter


class AndroidRouter(private val context: Context, private val parameterHolder: ActivityParameterHolder, private val activityTracker: CurrentActivityTracker) : IRouter {


    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = TagEntriesListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, tag, tagsFilter)
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
        showViewEntryView(EntryActivityParameters(entry))
    }

    override fun showViewEntryView(article: ReadLaterArticle) {
        showViewEntryView(EntryActivityParameters(readLaterArticle = article))
    }

    override fun showViewEntryView(extractionResult: EntryExtractionResult) {
        showViewEntryView(EntryActivityParameters(entryExtractionResult = extractionResult))
    }

    private fun showViewEntryView(parameters: EntryActivityParameters) {
        val viewArticleIntent = Intent(context, ViewEntryActivity::class.java)
        viewArticleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        addParametersToIntent(viewArticleIntent, parameters)

        context.startActivity(viewArticleIntent)
    }


    override fun showCreateEntryView() {
        showEditEntryView()
    }

    override fun showEditEntryView(entry: Entry, field: EntryField?) {
        showEditEntryView(EntryActivityParameters(entry, field = field))
    }

    override fun showEditEntryView(article: ReadLaterArticle, field: EntryField?) {
        showEditEntryView(EntryActivityParameters(readLaterArticle = article, field = field))
    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult, field: EntryField?) {
        showEditEntryView(EntryActivityParameters(entryExtractionResult = extractionResult, field = field))
    }

    private fun showEditEntryView(parameters: EntryActivityParameters? = null) {
        val editEntryIntent = Intent(context, EditEntryActivity::class.java)
        editEntryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        parameters?.let {
            val id = parameterHolder.setParameters(parameters)
            editEntryIntent.putExtra(BaseActivity.ParametersId, id)
        }

        context.startActivity(editEntryIntent)
    }


    override fun showCreateReferenceView() {
        showEditReferenceView(EditReferenceActivityParameters(null))
    }

    override fun showEditReferenceView(reference: Reference) {
        showEditReferenceView(EditReferenceActivityParameters(reference))
    }

    private fun showEditReferenceView(parameters: EditReferenceActivityParameters) {
        val editReferenceIntent = Intent(context, EditReferenceActivity::class.java)
        editReferenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        addParametersToIntent(editReferenceIntent, parameters)

        context.startActivity(editReferenceIntent)
    }


    override fun returnToPreviousView() {
        activityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { activity.onBackPressed() }
        }
    }


    private fun addParametersToIntent(intent: Intent, parameters: Any) {
        val id = parameterHolder.setParameters(parameters)

        intent.putExtra(BaseActivity.ParametersId, id)
    }

}