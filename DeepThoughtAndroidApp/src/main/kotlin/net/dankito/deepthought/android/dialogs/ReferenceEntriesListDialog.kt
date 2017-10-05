package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class ReferenceEntriesListDialog: EntriesListDialogBase() {

    companion object {
        val TAG: String = javaClass.name

        private const val REFERENCE_ID_EXTRA_NAME = "REFERENCE_ID"
    }


    @Inject
    protected lateinit var referenceService: ReferenceService


    private lateinit var reference: Reference

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)
    }


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, reference: Reference) {
        this.reference = reference

        showDialog(fragmentManager)
    }

    override fun onResume() {
        super.onResume()

        eventBus.register(eventBusListener)
    }

    override fun onPause() {
        eventBus.unregister(eventBusListener)

        super.onPause()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(REFERENCE_ID_EXTRA_NAME, reference.id)
        }
    }

    override fun restoreState(savedInstanceState: Bundle) {
        super.restoreState(savedInstanceState)

        savedInstanceState.getString(REFERENCE_ID_EXTRA_NAME)?.let { referenceId ->
            referenceService.retrieve(referenceId)?.let {
                this.reference = it
            }
        }
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