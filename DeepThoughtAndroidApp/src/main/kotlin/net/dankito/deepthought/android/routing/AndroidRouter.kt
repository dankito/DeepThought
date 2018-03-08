package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.activities.arguments.*
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.SourceItemsListDialog
import net.dankito.deepthought.android.dialogs.TagItemsListDialog
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.filechooserdialog.FileChooserDialog
import net.dankito.filechooserdialog.model.FileChooserDialogConfig
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
            val dialog = SourceItemsListDialog()
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
        showEditItemView(EditItemActivity::class.java, EditItemActivityParameters(createItem = true))
    }

    override fun showEditItemView(item: Item) {
        showEditItemView(EditItemActivity::class.java, EditItemActivityParameters(item))
    }

    override fun showEditItemView(article: ReadLaterArticle) {
        showEditItemView(EditReadLaterArticleActivity::class.java, EditItemActivityParameters(readLaterArticle = article))
    }

    override fun showEditItemView(extractionResult: ItemExtractionResult) {
        showEditItemView(EditItemExtractionResultActivity::class.java, EditItemActivityParameters(itemExtractionResult = extractionResult))
    }

    private fun showEditItemView(editItemActivityClass: Class<out EditItemActivityBase>, parameters: EditItemActivityParameters) {
        dataManager.addInitializationListener { // if you have a very large data set and are very, very quick, you can enter EditItemActivityBase before DataManager is initialized -> localSettings is null
            navigateToActivity(editItemActivityClass, parameters)
        }
    }


    override fun createItemFromPdf() {
        activityTracker.currentActivity?.let { activity ->
            val permissionsService = activity.registerPermissionsService()
            val config = FileChooserDialogConfig(listOf("pdf"), permissionToReadExternalStorageRationaleResourceId = R.string.open_file_permission_request_message)

            FileChooserDialog().showOpenSingleFileDialog(activity, permissionsService, config) { _, selectedFile ->
                selectedFile?.let {
                    showPdfView(it)
                }
            }
        }
    }


    override fun showEditSourceView(source: Source) {
        showEditSourceView(EditSourceActivityParameters(source))
    }

    override fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        showEditSourceView(EditSourceActivityParameters(source, series, editedSourceTitle))
    }

    private fun showEditSourceView(parameters: EditSourceActivityParameters) {
        navigateToActivity(EditSourceActivity::class.java, parameters)
    }


    override fun showEditSeriesView(series: Series) {
        showEditSeriesView(EditSeriesActivityParameters(series))
    }

    override fun showEditSourceSeriesView(forSource: Source, series: Series?) {
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