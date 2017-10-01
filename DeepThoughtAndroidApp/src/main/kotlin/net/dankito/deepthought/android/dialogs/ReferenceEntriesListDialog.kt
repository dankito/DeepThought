package net.dankito.deepthought.android.dialogs

import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler


class ReferenceEntriesListDialog: EntriesListDialogBase() {

    companion object {
        val TAG: String = javaClass.name
    }


    private lateinit var reference: Reference

    private val eventBusListener = EventBusListener()


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, reference: Reference) {
        this.reference = reference

        eventBus.register(eventBusListener)

        showDialog(fragmentManager)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        eventBus.unregister(eventBusListener)

        super.onDismiss(dialog)
    }


    override fun retrieveEntries(callback: (List<Entry>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch(entriesMustHaveThisReference = reference) {
            callback(it)
        })
    }

    override fun getDialogTitle(entries: List<Entry>): String {
        return reference.title
    }


    inner class EventBusListener {

        @Handler
        fun tagChanged(referenceChanged: ReferenceChanged) {
            if(referenceChanged.entity.id == reference.id) {
                retrieveAndShowEntries()
            }
        }

    }

}