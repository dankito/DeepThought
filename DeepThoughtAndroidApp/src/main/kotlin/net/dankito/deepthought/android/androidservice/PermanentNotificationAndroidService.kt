package net.dankito.deepthought.android.androidservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import net.dankito.deepthought.android.di.AppComponent
import javax.inject.Inject


class PermanentNotificationAndroidService: Service() {

    @Inject
    protected lateinit var permanentNotificationService: PermanentNotificationService


    init {
        AppComponent.component.inject(this)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(permanentNotificationService.handlesIntent(intent)) {

        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}