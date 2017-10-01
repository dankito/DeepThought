package net.dankito.deepthought.android.dialogs

import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.data.messages.TagChanged
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler


class TagEntriesListDialog : EntriesListDialogBase() {

    companion object {
        val TAG: String = javaClass.name
    }


    private lateinit var tag: Tag

    private lateinit var tagsFilter: List<Tag>

    private val eventBusListener = EventBusListener()


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, tag: Tag, tagsFilter: List<Tag> = listOf()) {
        this.tag = tag
        this.tagsFilter = tagsFilter

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
            searchEngine.searchEntries(EntriesSearch(entriesMustHaveTheseTags = mutableListOf(tag, *tagsFilter.toTypedArray())) {
                callback(it)
            })
        }
    }

    override fun getDialogTitle(entries: List<Entry>): String {
        return tag.displayText
    }


    inner class EventBusListener {

        @Handler
        fun tagChanged(tagChanged: TagChanged) {
            if(tagChanged.entity == tag) {
                retrieveAndShowEntries()
            }
        }

        @Handler
        fun entriesChanged(entriesChanged: EntryChanged) {
            retrieveAndShowEntries()
        }

    }

}