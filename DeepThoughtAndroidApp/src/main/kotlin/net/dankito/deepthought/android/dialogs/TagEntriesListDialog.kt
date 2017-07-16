package net.dankito.deepthought.android.dialogs

import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.TagChanged
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler


class TagEntriesListDialog : EntriesListDialogBase() {

    private lateinit var tag: Tag

    private var restrictToEntries: List<Entry>? = null

    private val eventBusListener = EventBusListener()


    fun showDialog(fragmentManager: FragmentManager, tag: Tag, restrictToEntries: List<Entry>? = null) {
        this.tag = tag
        this.restrictToEntries = restrictToEntries

        eventBus.register(eventBusListener)

        showDialog(fragmentManager)
    }


    override fun onDismiss(dialog: DialogInterface?) {
        eventBus.unregister(eventBusListener)

        super.onDismiss(dialog)
    }


    override fun retrieveEntries(callback: (List<Entry>) -> Unit) {
        if(tag is CalculatedTag) {
            callback(tag.entries)
        }
        else {
            searchEngine.searchEntries(EntriesSearch(entriesMustHaveTheseTags = mutableListOf(tag)) {
                callback(it)
            })
        }
    }


    inner class EventBusListener {

        @Handler
        fun tagChanged(tagChanged: TagChanged) {
            if(tagChanged.entity == tag) {
                retrieveAndShowEntries()
            }
        }

        @Handler
        fun entriesChanged(entriesChanged: EntitiesOfTypeChanged) {
            if(entriesChanged.entityType == Entry::class.java) {
                retrieveAndShowEntries()
            }
        }

    }

}