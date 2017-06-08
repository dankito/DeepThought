package net.dankito.deepthought.android.di

import dagger.Component
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewArticleActivity
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ActivitiesModule::class, CommonModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
    }


    fun inject(mainActivity: MainActivity)

    fun inject(articleSummaryActivity: ArticleSummaryActivity)

    fun inject(viewArticleActivity: ViewArticleActivity)

}