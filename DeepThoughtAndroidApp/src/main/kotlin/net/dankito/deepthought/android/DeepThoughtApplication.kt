package net.dankito.deepthought.android

import android.support.multidex.MultiDexApplication
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerAppComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule


class DeepThoughtApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        val component = DaggerAppComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(this))
                .build()

        CommonComponent.component = component
        AppComponent.component = component
    }

}