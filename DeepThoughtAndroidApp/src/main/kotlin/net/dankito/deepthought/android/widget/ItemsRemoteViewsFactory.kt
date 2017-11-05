package net.dankito.deepthought.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.preview
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject


class ItemsRemoteViewsFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var eventBus: IEventBus


    private val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    private var items: List<Item> = ArrayList()

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    override fun onCreate() {
        retrieveItems()
    }


    override fun onDataSetChanged() {
        retrieveItems()
    }

    private fun retrieveItems() {
        val countDownLatch = CountDownLatch(1)

        searchEngine.addInitializationListener {
            searchEngine.searchEntries(EntriesSearch {
                items = it
                countDownLatch.countDown()
            })
        }

        try {
            countDownLatch.await()
        } catch (ignored: Exception) { }
    }


    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]

        val remoteViews = RemoteViews(context.packageName, R.layout.items_widget_list_item)

        setData(item, remoteViews)

        return remoteViews
    }

    private fun setData(item: Item, remoteViews: RemoteViews) {
        var referencePreview = item.source.preview
        if (referencePreview.isNullOrBlank() && item.summary.isNullOrBlank() == false) {
            referencePreview = item.abstractPlainText
        }

        remoteViews.setViewVisibility(R.id.txtReferencePreview, if (referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE)
        remoteViews.setTextViewText(R.id.txtReferencePreview, referencePreview)

        remoteViews.setTextViewText(R.id.txtItemPreview, item.getEntryPreviewWithSeriesAndPublishingDate(item.source))

        remoteViews.setViewVisibility(R.id.txtItemTags, if (item.hasTags()) View.VISIBLE else View.GONE)
        remoteViews.setTextViewText(R.id.txtItemTags, item.tags.joinToString { it.name })
    }

    override fun onDestroy() {
        eventBus.unregister(eventBusListener)
    }


    private fun itemsChanged() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager?.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lstItems) // don't know why but calling notifyAppWidgetViewDataChanged() actually has no effect (onDataSetChanged() doesn't get called)

        retrieveItems() // so i have to call retrieveItems() directly
    }


    inner class EventBusListener {

        @Handler
        fun itemChanged(change: EntryChanged) {
            itemsChanged()
        }

    }

}