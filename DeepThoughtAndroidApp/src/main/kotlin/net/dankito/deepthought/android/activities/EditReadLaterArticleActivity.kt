package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.utils.android.extensions.HtmlExtensions
import net.dankito.utils.ui.dialogs.ConfirmationDialogConfig
import net.engio.mbassy.listener.Handler


class EditReadLaterArticleActivity : EditItemActivityBase() {

    companion object {
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
    }


    private lateinit var readLaterArticle: ReadLaterArticle


    private var eventBusListener: EventBusListener? = null


    private var mnDeleteReadLaterArticle: MenuItem? = null


    override fun getItemExtractionResult(): ItemExtractionResult? {
        return readLaterArticle.itemExtractionResult
    }


    override fun onResume() {
        super.onResume()

        mayRegisterEventBusListener()
    }

    override fun onPause() {
        unregisterEventBusListener()

        super.onPause()
    }


    override fun showParameters(parameters: EditItemActivityParameters) {
        parameters.readLaterArticle?.let {
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
        unregisterEventBusListener()

        presenter.deleteReadLaterArticle(readLaterArticle)

        runOnUiThread { closeDialog() }
    }


    override fun beforeSavingItem() {
        mnSaveItemExtractionResultForLaterReading?.isEnabled = false
        unregisterEventBusListener()
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
            mayRegisterEventBusListener()

            super.savingItemDone(successful)
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

            dialogService.showConfirmationDialog(HtmlExtensions.getSpannedFromHtml(this, R.string.context_help_saved_read_later_article_is_now_in_items),
                    config = ConfirmationDialogConfig(false)) {
                callback()
            }
        }
        else {
            callback()
        }
    }


    private fun mayRegisterEventBusListener() {
        if(eventBusListener == null) {
            synchronized(this) {
                val eventBusListenerInit = EventBusListener()

                eventBus.register(eventBusListenerInit)

                this.eventBusListener = eventBusListenerInit
            }
        }
    }

    private fun unregisterEventBusListener() {
        synchronized(this) {
            eventBusListener?.let {
                eventBus.unregister(it)
            }

            this.eventBusListener = null
        }
    }

    private fun warnReadLaterArticleHasBeenEdited() {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_item_alert_message_read_later_article_has_been_edited))
        }
    }


    inner class EventBusListener {

        @Handler
        fun itemChanged(change: ReadLaterArticleChanged) {
            if(change.entity.id == readLaterArticle.id) {
                if(change.source == EntityChangeSource.Synchronization && change.isDependentChange == false) {
                    warnReadLaterArticleHasBeenEdited()
                }
            }
        }
    }

}