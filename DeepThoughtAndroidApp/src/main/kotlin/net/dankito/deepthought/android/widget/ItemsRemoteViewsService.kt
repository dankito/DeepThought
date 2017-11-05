package net.dankito.deepthought.android.widget

import android.content.Intent
import android.widget.RemoteViewsService


class ItemsRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ItemsRemoteViewsFactory(this, intent)
    }

}