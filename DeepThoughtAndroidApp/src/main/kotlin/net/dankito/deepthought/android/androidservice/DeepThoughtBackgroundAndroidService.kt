package net.dankito.deepthought.android.androidservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.slf4j.LoggerFactory


class DeepThoughtBackgroundAndroidService : Service() {

    companion object {
        private val log = LoggerFactory.getLogger(DeepThoughtBackgroundAndroidService::class.java)
    }


    private var binder: IBinder? = null


    override fun onCreate() {
        super.onCreate()

        if (binder == null) {
            log.info("Instantiating DeepThoughtBackgroundAndroidService ...")
            binder = DeepThoughtBackgroundAndroidServiceBinder(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        log.info("Destroying DeepThoughtBackgroundAndroidService ...")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

}