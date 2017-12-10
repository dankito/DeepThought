package net.dankito.deepthought.android.di

import dagger.Component
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.activities.EditItemActivity_EditSourceTest
import net.dankito.deepthought.android.play_store.CreatePlayStoreScreenShots
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

    fun inject(editItemActivity_EditSourceTest: EditItemActivity_EditSourceTest)

    fun inject(createPlayStoreScreenShots: CreatePlayStoreScreenShots)

}