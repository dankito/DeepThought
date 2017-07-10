package net.dankito.deepthought.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.multidex.MultiDexApplication
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidService
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidServiceBinder


class DeepThoughtApplication : MultiDexApplication() {

    private var service: DeepThoughtBackgroundAndroidService? = null


    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, DeepThoughtBackgroundAndroidService::class.java)
        startService(intent)
        bindService(intent, deepThoughtBackgroundServiceConnection, Context.BIND_AUTO_CREATE)
    }


    private val deepThoughtBackgroundServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val backgroundServiceBinder = binder as DeepThoughtBackgroundAndroidServiceBinder
            service = backgroundServiceBinder.service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service = null
        }

    }

}