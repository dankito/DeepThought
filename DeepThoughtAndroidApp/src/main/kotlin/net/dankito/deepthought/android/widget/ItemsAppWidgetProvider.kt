package net.dankito.deepthought.android.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import net.dankito.deepthought.android.R


class ItemsAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val remoteViews = getRemoteViews(context, appWidgetManager, widgetId)

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.lstItems)

        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    private fun getRemoteViews(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int): RemoteViews {
        // Set up the intent that starts the ItemsRemoteViewsService, which will
        // provide the views for this collection.
        val intent = Intent(context, ItemsRemoteViewsService::class.java)
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        // Instantiate the RemoteViews object for the app widget layout.
        val remoteViews = RemoteViews(context.packageName, R.layout.items_widget_layout)
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        remoteViews.setRemoteAdapter(R.id.lstItems, intent)

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        remoteViews.setEmptyView(R.id.empty_view, R.id.empty_view)

        //
        // Do additional processing specific to this app widget...
        //

        return remoteViews
    }

}