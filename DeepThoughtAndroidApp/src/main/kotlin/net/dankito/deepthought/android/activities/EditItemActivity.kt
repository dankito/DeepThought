package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.model.Item
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.ItemChanged
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.engio.mbassy.listener.Handler


class EditItemActivity : EditItemActivityBase() {

    companion object {
        private const val ITEM_ID_INTENT_EXTRA_NAME = "ITEM_ID"
    }


    private lateinit var item: Item


    private var eventBusListener: EventBusListener? = null


    override fun onResume() {
        super.onResume()

        mayRegisterEventBusListener()
    }

    override fun onPause() {
        unregisterEventBusListener()

        super.onPause()
    }


    override fun showParameters(parameters: EditItemActivityParameters) {
        parameters.item?.let { editItem(it) }

        if(parameters.createItem) {
            createItem()
        }
    }

    override fun restoreEntity(savedInstanceState: Bundle) {
        if(savedInstanceState.containsKey(ITEM_ID_INTENT_EXTRA_NAME)) {
            savedInstanceState.getString(ITEM_ID_INTENT_EXTRA_NAME)?.let { itemId -> editItem(itemId) }
        }
        else {
            createItem(false) // don't go to EditHtmlTextDialog for content here as we're restoring state, content may already be set
        }
    }

    private fun createItem(editContent: Boolean = true) {
        editItem(Item(""))

        if(editContent) {
            editContent() // go directly to edit content dialog, there's absolutely nothing to see on this almost empty screen
        }
    }

    private fun editItem(itemId: String) {
        itemService.retrieve(itemId)?.let { item ->
            editItem(item)
        }
    }

    private fun editItem(item: Item) {
        this.item = item

        mnDeleteExistingItem?.isVisible = item.isPersisted()

        editItem(item, item.source, item.source?.series, item.tags, item.attachedFiles)
    }


    override fun saveState(outState: Bundle) {
        item.id?.let { itemId -> outState.putString(ITEM_ID_INTENT_EXTRA_NAME, itemId) }
    }


    override fun adjustViewHtmlOptionsMenu(menu: Menu) {
        mnDeleteExistingItem = menu.findItem(R.id.mnDeleteExistingItem)
        mnDeleteExistingItem?.isVisible = item.isPersisted()
    }

    override fun isEntitySavable(): Boolean {
        return haveAllFieldsBeenCleared() == false && hasUnsavedChanges
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnDeleteExistingItem -> {
                askIfShouldDeleteExistingItemAndCloseDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun askIfShouldDeleteExistingItemAndCloseDialog() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_delete_item, item.preview)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                mnDeleteExistingItem?.isEnabled = false
                unregisterEventBusListener()

                deleteEntityService.deleteItem(item) // TODO: move to presenter
                closeDialog()
            }
        }
    }

    override fun beforeSavingItem() {
        unregisterEventBusListener()
    }

    override fun savingItemDone(successful: Boolean) {
        if(successful == false) {
            mayRegisterEventBusListener()
        }

        super.savingItemDone(successful)
    }


    private fun mayRegisterEventBusListener() {
        if(item.isPersisted() == true && eventBusListener == null) {
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

    private fun warnItemHasBeenEdited() {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_item_alert_message_item_has_been_edited))
        }
    }


    inner class EventBusListener {

        @Handler
        fun itemChanged(change: ItemChanged) { // TODO: what about ReadLaterArticle?
            if(change.entity.id == item.id) {
                if(change.source == EntityChangeSource.Synchronization && change.isDependentChange == false) {
                    warnItemHasBeenEdited()
                }
            }
        }
    }

}