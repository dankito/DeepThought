package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityParameters
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.ReferenceEntriesListDialog
import net.dankito.deepthought.android.dialogs.TagEntriesListDialog
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.utils.serialization.ISerializer


class AndroidRouter(private val context: Context, private val parameterHolder: ActivityParameterHolder, private val activityTracker: CurrentActivityTracker, private val serializer: ISerializer) : IRouter {


    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = TagEntriesListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, tag, tagsFilter)
        }
    }

    override fun showEntriesForReference(source: Source) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = ReferenceEntriesListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, source)
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

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary?) {
        val articleSummaryActivityIntent = Intent(context, ArticleSummaryActivity::class.java)
        articleSummaryActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        articleSummaryActivityIntent.putExtra(ArticleSummaryActivity.EXTRACTOR_URL_INTENT_EXTRA_NAME, extractor.url)

        summary?.let {
            articleSummaryActivityIntent.putExtra(ArticleSummaryActivity.LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, serializer.serializeObject(summary))
        }

        context.startActivity(articleSummaryActivityIntent)
    }

    override fun showReadLaterArticlesView() {
        // is a tab on main screen in Android, no need for routing
    }


    override fun showCreateEntryView() {
        showEditEntryView(EditEntryActivityParameters(createEntry = true))
    }

    override fun showEditEntryView(item: Item) {
        showEditEntryView(EditEntryActivityParameters(item))
    }

    override fun showEditEntryView(article: ReadLaterArticle) {
        showEditEntryView(EditEntryActivityParameters(readLaterArticle = article))
    }

    override fun showEditEntryView(extractionResult: ItemExtractionResult) {
        showEditEntryView(EditEntryActivityParameters(itemExtractionResult = extractionResult))
    }

    private fun showEditEntryView(parameters: EditEntryActivityParameters) {
        val editEntryIntent = Intent(context, EditEntryActivity::class.java)
        editEntryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        addParametersToIntent(editEntryIntent, parameters)

        context.startActivity(editEntryIntent)
    }


    override fun showEditReferenceView(source: Source) {
        showEditReferenceView(EditReferenceActivityParameters(source))
    }

    override fun showEditEntryReferenceView(forItem: Item, source: Source?, series: Series?) {
        showEditReferenceView(EditReferenceActivityParameters(source, forItem, series))
    }

    private fun showEditReferenceView(parameters: EditReferenceActivityParameters) {
        val editReferenceIntent = Intent(context, EditReferenceActivity::class.java)
        editReferenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        addParametersToIntent(editReferenceIntent, parameters)

        context.startActivity(editReferenceIntent)
    }


    override fun showEditSeriesView(series: Series) {
        showEditSeriesView(EditSeriesActivityParameters(series))
    }

    override fun showEditReferenceSeriesView(forSource: Source, series: Series?) {
        showEditSeriesView(EditSeriesActivityParameters(series, forSource))
    }

    private fun showEditSeriesView(parameters: EditSeriesActivityParameters) {
        val editSeriesIntent = Intent(context, EditSeriesActivity::class.java)
        editSeriesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        addParametersToIntent(editSeriesIntent, parameters)

        context.startActivity(editSeriesIntent)
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