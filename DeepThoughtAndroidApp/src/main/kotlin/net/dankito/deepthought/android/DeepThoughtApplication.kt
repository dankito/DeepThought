package net.dankito.deepthought.android

import android.support.multidex.MultiDexApplication
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerAppComponent
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject


class DeepThoughtApplication : MultiDexApplication() {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine


    override fun onCreate() {
        super.onCreate()

        val component = DaggerAppComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(this))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.component = component

        // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
        // speeding app start up a bit.
        // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices
        component.inject(this)
    }

}