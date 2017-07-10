package net.dankito.deepthought.android.androidservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerAppComponent
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import org.slf4j.LoggerFactory
import javax.inject.Inject


class DeepThoughtBackgroundAndroidService : Service() {

    companion object {
        private val log = LoggerFactory.getLogger(DeepThoughtBackgroundAndroidService::class.java)
    }


    @Inject
    protected lateinit var appInitializer: AndroidAppInitializer

    private var binder: IBinder? = null


    override fun onCreate() {
        super.onCreate()

        if (binder == null) {
            log.info("Instantiating DeepThoughtBackgroundAndroidService ...")
            binder = DeepThoughtBackgroundAndroidServiceBinder(this)

            setupDependencyInjection()

            setupLogic()
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


    private fun setupDependencyInjection() {
        val component = DaggerAppComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(this))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.setComponentInstance(component)

        component.inject(this)
    }


    private fun setupLogic() {
        appInitializer.initializeApp()
    }

}