package net.dankito.deepthought.android.di

import dagger.Component
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewEntryActivity
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorIconsAdapter
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidService
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.appstart.CommunicationManagerStarter
import net.dankito.deepthought.android.dialogs.AddArticleSummaryExtractorDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorConfigDialog
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorsDialog
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.ReferencesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.service.di.AndroidCommonModule
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ActivitiesModule::class, AndroidCommonModule::class, CommonModule::class, CommonDataModule::class, BaseModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
            private set

        fun setComponentInstance(component: AppComponent) {
            AppComponent.component = component

            appComponentInitialized()
        }

        var isInitialized = false
            private set

        private val initializationListeners = mutableSetOf<() -> Unit>()

        fun addInitializationListener(listener: () -> Unit) {
            if(isInitialized) {
                callInitializationListener(listener)
            }
            else {
                initializationListeners.add(listener)
            }
        }

        private fun appComponentInitialized() {
            isInitialized = true

            for(listener in HashSet<() -> Unit>(initializationListeners)) {
                callInitializationListener(listener)
            }

            initializationListeners.clear()
        }

        private fun callInitializationListener(listener: () -> Unit) {
            listener()
        }
    }


    fun inject(backgroundService: DeepThoughtBackgroundAndroidService)

    fun inject(app: DeepThoughtApplication)

    fun inject(mainActivity: MainActivity)

    fun inject(appInitializer: AndroidAppInitializer)

    fun inject(communicationManagerStarter: CommunicationManagerStarter)

    fun inject(entriesListView: EntriesListView)

    fun inject(tagsListView: TagsListView)

    fun inject(referencesListView: ReferencesListView)

    fun inject(readLaterArticlesListView: ReadLaterArticlesListView)

    fun inject(addArticleSummaryExtractorDialog: AddArticleSummaryExtractorDialog)

    fun inject(articleSummaryExtractorIconsAdapter: ArticleSummaryExtractorIconsAdapter)

    fun inject(articleSummaryExtractorsDialog: ArticleSummaryExtractorsDialog)

    fun inject(articleSummaryExtractorsAdapter: ArticleSummaryExtractorsAdapter)

    fun inject(articleSummaryExtractorConfigDialog: ArticleSummaryExtractorConfigDialog)

    fun inject(articleSummaryActivity: ArticleSummaryActivity)

    fun inject(viewEntryActivity: ViewEntryActivity)

}