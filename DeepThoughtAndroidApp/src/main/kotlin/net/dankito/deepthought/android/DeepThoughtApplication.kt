package net.dankito.deepthought.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.multidex.MultiDexApplication
import com.squareup.leakcanary.LeakCanary
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidService
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidServiceBinder
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerAppComponent
import net.dankito.deepthought.android.service.AppLifeCycleListener
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import javax.inject.Inject


open class DeepThoughtApplication : MultiDexApplication() {

    @Inject
    protected lateinit var appInitializer: AndroidAppInitializer

    @Inject
    protected lateinit var lifeCycleListener: AppLifeCycleListener


    private var service: DeepThoughtBackgroundAndroidService? = null


    override fun onCreate() {
        super.onCreate()

        if(LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)


        val component = setupDependencyInjection()
        component.inject(this)

        setupLogic()

        val intent = Intent(this, DeepThoughtBackgroundAndroidService::class.java)
        startService(intent)
        bindService(intent, deepThoughtBackgroundServiceConnection, Context.BIND_AUTO_CREATE)
    }


    protected open fun setupDependencyInjection(): AppComponent {
        val component = DaggerAppComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(this))
                .build()

        setComponent(component)

        return component
    }

    protected open fun setComponent(component: AppComponent) {
        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.setComponentInstance(component)
    }


    private fun setupLogic() {
        registerActivityLifecycleCallbacks(lifeCycleListener)
        registerComponentCallbacks(lifeCycleListener)

        appInitializer.initializeApp()
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