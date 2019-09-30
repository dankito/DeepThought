package net.dankito.deepthought.android.routing

import android.content.Context
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.dialogs.SourceItemsListDialog
import net.dankito.deepthought.android.dialogs.TagItemsListDialog
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.windowdata.*
import net.dankito.filechooserdialog.FileChooserDialog
import net.dankito.filechooserdialog.model.FileChooserDialogConfig
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.utils.android.ui.activities.ActivityParameterHolder
import net.dankito.utils.windowregistry.android.ui.extensions.currentAndroidWindow
import net.dankito.utils.windowregistry.android.ui.router.AndroidRouterBase
import net.dankito.utils.windowregistry.window.WindowRegistry
import java.io.File


class AndroidRouter(context: Context, windowRegistry: WindowRegistry, parameterHolder: ActivityParameterHolder, private val dataManager: DataManager)
    : AndroidRouterBase(context, windowRegistry, parameterHolder), IRouter {


    override fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>) {
        windowRegistry.currentAndroidWindow?.let { currentAndroidWindow ->
            val dialog = TagItemsListDialog()
            dialog.showDialog(currentAndroidWindow.supportFragmentManager, tag, tagsFilter)
        }
    }

    override fun showItemsForSource(source: Source) {
        windowRegistry.currentAndroidWindow?.let { currentAndroidWindow ->
            val dialog = SourceItemsListDialog()
            dialog.showDialog(currentAndroidWindow.supportFragmentManager, source)
        }
    }


    override fun showArticleSummaryExtractorsView() {
        windowRegistry.currentAndroidWindow?.let { currentAndroidWindow ->
            val articleSummaryExtractorsDialog = ArticleSummaryExtractorsDialog(currentAndroidWindow)
            articleSummaryExtractorsDialog.showDialog()
        }
    }

    override fun showAddArticleSummaryExtractorView() {
        windowRegistry.currentAndroidWindow?.supportFragmentManager?.let { fragmentManager ->
            val addArticleSummaryExtractorDialog = AddArticleSummaryExtractorDialog()

            addArticleSummaryExtractorDialog.show(fragmentManager, AddArticleSummaryExtractorDialog.TAG)
        }
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary?) {
        navigateToActivity(ArticleSummaryActivity::class.java, ArticleSummaryWindowData(extractor, summary))
    }

    override fun showReadLaterArticlesView() {
        // is a tab on main screen in Android, no need for routing
    }


    override fun showCreateItemView() {
        showEditItemView(EditItemActivity::class.java, EditItemWindowData(Item("")))
    }

    override fun showEditItemView(item: Item) {
        showEditItemView(EditItemActivity::class.java, EditItemWindowData(item))
    }

    override fun showEditItemView(article: ReadLaterArticle) {
        showEditItemView(EditReadLaterArticleActivity::class.java, EditReadLaterArticleWindowData(article))
    }

    override fun showEditItemView(extractionResult: ItemExtractionResult) {
        showEditItemView(EditItemExtractionResultActivity::class.java, EditItemExtractionResultWindowData(extractionResult))
    }

    private fun showEditItemView(editItemActivityClass: Class<out EditItemActivityBase>, parameters: EditItemWindowDataBase) {
        dataManager.addInitializationListener { // if you have a very large data set and are very, very quick, you can enter EditItemActivityBase before DataManager is initialized -> localSettings is null
            navigateToActivity(editItemActivityClass, parameters)
        }
    }


    override fun createItemFromPdf() {
        (windowRegistry.currentAndroidWindow as? BaseActivity)?.let { activity ->
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
        showEditSourceView(EditSourceWindowData(source))
    }

    override fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        val sourceToEdit = source ?: Source("")

        showEditSourceView(EditSourceWindowData(sourceToEdit, series, editedSourceTitle))
    }

    private fun showEditSourceView(parameters: EditSourceWindowData) {
        navigateToActivity(EditSourceActivity::class.java, parameters)
    }


    override fun showPdfView(addNewPdfFile: File, sourceForFile: Source?) {
        showImportFromPdfView(ViewPdfWindowData(addNewPdfFile, null, sourceForFile))
    }

    override fun showPdfView(persistedPdfFile: FileLink, sourceForFile: Source?) {
        showImportFromPdfView(ViewPdfWindowData(null, persistedPdfFile, sourceForFile))
    }

    private fun showImportFromPdfView(parameters: ViewPdfWindowData) {
        navigateToActivity(ViewPdfActivity::class.java, parameters)
    }


    override fun returnToPreviousView() {
        windowRegistry.currentAndroidWindow?.let { activity ->
            activity.runOnUiThread { activity.onBackPressed() }
        }
    }


    private fun navigateToActivity(activityClass: Class<out BaseActivity>, parameters: Any? = null) {

        showWindow(activityClass, parameters?.let { it.javaClass }, parameters)

//        val intent = Intent(context, activityClass)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//        parameters?.let { addParametersToIntent(intent, parameters) }
//
//        context.startActivity(intent)
    }

//    private fun addParametersToIntent(intent: Intent, parameters: Any) {
//        val id = parameterHolder.setParameters(parameters)
//
//        intent.putExtra(BaseActivity.ParametersId, id)
//    }

}