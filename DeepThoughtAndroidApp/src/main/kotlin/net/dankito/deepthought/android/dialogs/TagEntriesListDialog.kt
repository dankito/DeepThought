package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.AllCalculatedTags
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import net.dankito.service.data.messages.ItemChanged
import net.dankito.service.data.messages.TagChanged
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class TagEntriesListDialog : EntriesListDialogBase() {

    companion object {
        val TAG: String = javaClass.name

        private const val TAG_ID_EXTRA_NAME = "TAG_ID"

        private const val TAG_FILTER_IDS_EXTRA_NAME = "TAG_FILTER_IDS"

        private const val CALCULATED_TAG_NAME_EXTRA_NAME = "CALCULATED_TAG_NAME"
    }


    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var allCalculatedTags: AllCalculatedTags


    private var tag: Tag? = null // made it nullable instead of lateinit so that at least application doesn't crash if it cannot be set on restore

    private var tagsFilter: List<Tag> = ArrayList()

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)
    }


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, tag: Tag, tagsFilter: List<Tag> = listOf()) {
        setupDialog(tag, tagsFilter)

        showDialog(fragmentManager)
    }

    private fun setupDialog(tag: Tag, tagsFilter: List<Tag>) {
        this.tag = tag
        this.tagsFilter = tagsFilter
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
            if(tag is CalculatedTag) {
                outState.putString(CALCULATED_TAG_NAME_EXTRA_NAME, tag?.name)
            }
            else {
                outState.putString(TAG_ID_EXTRA_NAME, tag?.id)
            }

            outState.putString(TAG_FILTER_IDS_EXTRA_NAME, tagsFilter.filterNotNull().joinToString(",") { it.id ?: "" })
        }
    }

    override fun restoreState(savedInstanceState: Bundle) {
        super.restoreState(savedInstanceState)

        var tag: Tag? = null

        savedInstanceState.getString(TAG_ID_EXTRA_NAME)?.let { tagId ->
            tag = tagService.retrieve(tagId)
        }
        savedInstanceState.getString(CALCULATED_TAG_NAME_EXTRA_NAME)?.let { calculatedTagName ->
            tag = allCalculatedTags.getCalculatedTagForName(calculatedTagName)
        }


        val tagsFilter = ArrayList<Tag>()
        savedInstanceState.getString(TAG_FILTER_IDS_EXTRA_NAME)?.split(",")?.map { it.trim() }?.forEach {
            tagService.retrieve(it)?.let { tagsFilter.add(it) }
        }

        tag?.let { restoredTag ->
            setupDialog(restoredTag, tagsFilter)
        }
    }


    override fun retrieveEntries(callback: (List<Item>) -> Unit) {
        tag?.let {
            retrieveEntries(it, callback)
        }

        if(tag == null) {
            callback(ArrayList<Item>())
        }
    }

    private fun retrieveEntries(tag: Tag, callback: (List<Item>) -> Unit) {
        if(tag is CalculatedTag) {
            callback(tag.items)
        }
        else {
            searchEngine.searchEntries(EntriesSearch(entriesMustHaveTheseTags = mutableListOf(tag, *tagsFilter.toTypedArray())) {
                callback(it)
            })
        }
    }

    override fun getDialogTitle(items: List<Item>): String {
        tag?.let {
            return it.name + " (" + items.size + ")"
        }

        return super.getDialogTitle(items)
    }


    inner class EventBusListener {

        @Handler
        fun tagChanged(tagChanged: TagChanged) {
            if(tagChanged.entity.id == tag?.id) {
                retrieveAndShowItems()
            }
        }

        @Handler
        fun itemChanged(itemChanged: ItemChanged) {
            retrieveAndShowItems()
        }

    }

}