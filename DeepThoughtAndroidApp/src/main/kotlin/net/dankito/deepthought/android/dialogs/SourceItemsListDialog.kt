package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.service.data.SourceService
import net.dankito.service.data.messages.ItemChanged
import net.dankito.service.data.messages.SourceChanged
import net.dankito.service.search.specific.ItemsSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class SourceItemsListDialog : ItemsListDialogBase() {

    companion object {
        val TAG: String = javaClass.name

        private const val SOURCE_ID_EXTRA_NAME = "SOURCE_ID"
    }


    @Inject
    protected lateinit var sourceService: SourceService


    private var source: Source? = null // made it nullable instead of lateinit so that at least application doesn't crash if it cannot be set on restore

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)
    }


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, source: Source) {
        this.source = source

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
            outState.putString(SOURCE_ID_EXTRA_NAME, source?.id)
        }
    }

    override fun restoreState(savedInstanceState: Bundle) {
        super.restoreState(savedInstanceState)

        savedInstanceState.getString(SOURCE_ID_EXTRA_NAME)?.let { sourceId ->
            sourceService.retrieve(sourceId)?.let {
                this.source = it
            }
        }
    }


    override fun retrieveItems(callback: (List<Item>) -> Unit) {
        searchEngine.searchItems(ItemsSearch(itemsMustHaveThisSource = source) {
            callback(it)
        })
    }

    override fun getDialogTitle(items: List<Item>): String {
        return source?.title ?: super.getDialogTitle(items)
    }


    inner class EventBusListener {

        @Handler
        fun sourceChanged(sourceChanged: SourceChanged) {
            if(sourceChanged.entity.id == source?.id) {
                retrieveAndShowItems()
            }
        }

        @Handler
        fun itemChanged(itemChanged: ItemChanged) {
            retrieveAndShowItems()
        }

    }

}