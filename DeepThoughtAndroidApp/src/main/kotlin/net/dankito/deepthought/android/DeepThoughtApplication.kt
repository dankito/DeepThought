package net.dankito.deepthought.android

import android.support.multidex.MultiDexApplication
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerAppComponent
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import javax.inject.Inject


class DeepThoughtApplication : MultiDexApplication() {

    @Inject
    protected lateinit var appInitializer: AndroidAppInitializer


    override fun onCreate() {
        super.onCreate()

        val component = DaggerAppComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(this))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.component = component

        component.inject(this)

        appInitializer.initializeApp()
    }

}