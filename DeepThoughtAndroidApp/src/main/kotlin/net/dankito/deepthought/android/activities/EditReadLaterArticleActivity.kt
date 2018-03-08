package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.ui.model.ConfirmationDialogConfig


class EditReadLaterArticleActivity : EditItemActivityBase() {

    companion object {
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
    }


    private lateinit var readLaterArticle: ReadLaterArticle


    private var mnDeleteReadLaterArticle: MenuItem? = null


    override fun getItemExtractionResult(): ItemExtractionResult? {
        return readLaterArticle.itemExtractionResult
    }


    override fun showParameters(parameters: EditItemActivityParameters) {
        parameters.readLaterArticle?.let {
            isInReaderMode = it.itemExtractionResult.couldExtractContent
            editReadLaterArticle(it)
        }
    }

    override fun restoreEntity(savedInstanceState: Bundle) {
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            editReadLaterArticle(readLaterArticle)
        }
    }

    private fun editReadLaterArticle(readLaterArticle: ReadLaterArticle, updateContentPreview: Boolean = true) {
        this.readLaterArticle = readLaterArticle
        val extractionResult = readLaterArticle.itemExtractionResult

        mnSaveItem?.setIcon(R.drawable.ic_tab_items)
        editItem(extractionResult.item, extractionResult.source, extractionResult.series, extractionResult.tags, extractionResult.files, updateContentPreview)
    }

    override fun saveState(outState: Bundle) {
        outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticle.id)
    }


    override fun adjustViewHtmlOptionsMenu(menu: Menu) {
        mnSaveItem?.setIcon(R.drawable.ic_tab_items)

        mnToggleReaderMode?.isVisible = readLaterArticle.itemExtractionResult.couldExtractContent == true // show mnToggleReaderMode only if original web site has been shown before

        mnDeleteReadLaterArticle = menu.findItem(R.id.mnDeleteReadLaterArticle)
        mnDeleteReadLaterArticle?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnDeleteReadLaterArticle -> {
                deleteReadLaterArticleAndCloseDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteReadLaterArticleAndCloseDialog() {
        mnSaveItem?.isEnabled = false // disable to that save cannot be pressed a second time
        mnDeleteReadLaterArticle?.isEnabled = false
//        unregisterEventBusListener()

        presenter.deleteReadLaterArticle(readLaterArticle)

        runOnUiThread { closeDialog() }
    }


    override fun beforeSavingItem() {
        mnSaveItemExtractionResultForLaterReading?.isEnabled = false
    }

    override fun resetSeries() {
        readLaterArticle.itemExtractionResult.series = null
    }

    override fun savingItemDone(successful: Boolean) {
        if(successful) {
            readLaterArticleService.delete(readLaterArticle)

            mayShowSavedReadLaterArticleHelpAndCloseDialog()
        }
        else {
            mnSaveItemExtractionResultForLaterReading?.isEnabled = true
        }
    }

    private fun mayShowSavedReadLaterArticleHelpAndCloseDialog() {
        mayShowSavedReadLaterArticleHelp {
            runOnUiThread {
                closeDialog()
            }
        }
    }

    private fun mayShowSavedReadLaterArticleHelp(callback: () -> Unit) {
        val localSettings = itemService.dataManager.localSettings

        if(localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp == false) {
            localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp = true
            itemService.dataManager.localSettingsUpdated()

            val helpText = getText(R.string.context_help_saved_read_later_article_is_now_in_items).toString()
            dialogService.showConfirmationDialog(contextHelpUtil.stringUtil.getSpannedFromHtml(helpText), config = ConfirmationDialogConfig(false)) {
                callback()
            }
        }
        else {
            callback()
        }
    }

}