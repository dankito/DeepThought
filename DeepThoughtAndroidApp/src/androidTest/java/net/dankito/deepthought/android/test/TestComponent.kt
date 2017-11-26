package net.dankito.deepthought.android.test

import dagger.Component
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AndroidCommonModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.FlavorModule
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ActivitiesModule::class, FlavorModule::class, AndroidCommonModule::class, CommonModule::class, CommonDataModule::class, BaseModule::class))
interface TestComponent : AppComponent {

    companion object {
        lateinit var component: TestComponent
            private set

        fun setComponentInstance(component: TestComponent) {
            TestComponent.component = component
        }
    }


    fun inject(deepThoughtAndroidTestBase: DeepThoughtAndroidTestBase)

}