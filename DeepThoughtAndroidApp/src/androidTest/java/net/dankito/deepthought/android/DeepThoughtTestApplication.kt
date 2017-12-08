package net.dankito.deepthought.android

import android.support.test.InstrumentationRegistry
import net.dankito.deepthought.android.di.*


class DeepThoughtTestApplication : DeepThoughtApplication() {

    override fun setupDependencyInjection(): AppComponent {
        val component = DaggerTestComponent.builder()
                .commonModule(UiTestCommonModule())
                .androidCommonModule(UiTestAndroidCommonModule())
                .activitiesModule(ActivitiesModule(InstrumentationRegistry.getTargetContext()))
                .build()

        setComponent(component)
        TestComponent.setComponentInstance(component)

        return component
    }

}