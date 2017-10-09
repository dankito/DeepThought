package net.dankito.deepthought.android

import android.app.Activity
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.rule.ActivityTestRule
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.test.DaggerTestComponent
import net.dankito.deepthought.android.test.TestComponent
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReadLaterArticleService
import javax.inject.Inject


class DeepThoughtActivityTestRule<T : Activity>(activityClass: Class<T>) : ActivityTestRule<T>(activityClass) {


    init {
        setupDi()
    }


    private fun setupDi() {
        val component = DaggerTestComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(getTargetContext()))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.setComponentInstance(component)
        TestComponent.setComponentInstance(component)
    }


}