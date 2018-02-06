package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.activities.arguments.*
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.ReferenceItemsListDialog
import net.dankito.deepthought.android.dialogs.TagItemsListDialog
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.ArticleSummary
import java.io.File


class AndroidRouter(private val context: Context, private val parameterHolder: ActivityParameterHolder, private val activityTracker: CurrentActivityTracker,
                    private val dataManager: DataManager) : IRouter {


    override fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = TagItemsListDialog()
            dialog.showDialog(currentActivity.supportFragmentManager, tag, tagsFilter)
        }
    }

    override fun showItemsForSource(source: Source) {
        activityTracker.currentActivity?.let { currentActivity ->
            val dialog = ReferenceItemsListDialog()
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
        navigateToActivity(ArticleSummaryActivity::class.java, ArticleSummaryActivityParameters(extractor, summary))
    }

    override fun showReadLaterArticlesView() {
        // is a tab on main screen in Android, no need for routing
    }


    override fun showCreateItemView() {
        showEditItemView(EditItemActivityParameters(createItem = true))
    }

    override fun showEditItemView(item: Item) {
        showEditItemView(EditItemActivityParameters(item))
    }

    override fun showEditItemView(article: ReadLaterArticle) {
        showEditItemView(EditItemActivityParameters(readLaterArticle = article))
    }

    override fun showEditItemView(extractionResult: ItemExtractionResult) {
        showEditItemView(EditItemActivityParameters(itemExtractionResult = extractionResult))
    }

    private fun showEditItemView(parameters: EditItemActivityParameters) {
        dataManager.addInitializationListener { // if you have a very large data set and are very, very quick, you can enter EditItemActivity before DataManager is initialized -> localSettings is null
            navigateToActivity(EditItemActivity::class.java, parameters)
        }
    }


    override fun showEditReferenceView(source: Source) {
        showEditReferenceView(EditReferenceActivityParameters(source))
    }

    override fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        showEditReferenceView(EditReferenceActivityParameters(source, series, editedSourceTitle))
    }

    private fun showEditReferenceView(parameters: EditReferenceActivityParameters) {
        navigateToActivity(EditReferenceActivity::class.java, parameters)
    }


    override fun showEditSeriesView(series: Series) {
        showEditSeriesView(EditSeriesActivityParameters(series))
    }

    override fun showEditReferenceSeriesView(forSource: Source, series: Series?) {
        showEditSeriesView(EditSeriesActivityParameters(series))
    }

    private fun showEditSeriesView(parameters: EditSeriesActivityParameters) {
        navigateToActivity(EditSeriesActivity::class.java, parameters)
    }


    override fun showPdfView(addNewPdfFile: File, sourceForFile: Source?) {
        showImportFromPdfView(ViewPdfActivityParameters(null, addNewPdfFile, sourceForFile))
    }

    override fun showPdfView(persistedPdfFile: FileLink, sourceForFile: Source?) {
        showImportFromPdfView(ViewPdfActivityParameters(persistedPdfFile, null, sourceForFile))
    }

    private fun showImportFromPdfView(parameters: ViewPdfActivityParameters) {
        navigateToActivity(ViewPdfActivity::class.java, parameters)
    }


    override fun returnToPreviousView() {
        activityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { activity.onBackPressed() }
        }
    }


    private fun navigateToActivity(activityClass: Class<out BaseActivity>, parameters: Any? = null) {
        val intent = Intent(context, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        parameters?.let { addParametersToIntent(intent, parameters) }

        context.startActivity(intent)
    }

    private fun addParametersToIntent(intent: Intent, parameters: Any) {
        val id = parameterHolder.setParameters(parameters)

        intent.putExtra(BaseActivity.ParametersId, id)
    }

}