package net.dankito.deepthought.android.di

import dagger.Component
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.EditEntryActivity
import net.dankito.deepthought.android.activities.EditReferenceActivity
import net.dankito.deepthought.android.activities.EditSeriesActivity
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorIconsAdapter
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidService
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.appstart.CommunicationManagerStarter
import net.dankito.deepthought.android.dialogs.*
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.ReferencesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.service.AndroidClipboardService
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.SnackbarService
import net.dankito.deepthought.android.service.network.NetworkConnectivityChangeBroadcastReceiver
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ActivitiesModule::class, FlavorModule::class, AndroidCommonModule::class, CommonModule::class, CommonDataModule::class, BaseModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
            private set

        fun setComponentInstance(component: AppComponent) {
            AppComponent.component = component
        }
    }


    fun inject(backgroundService: DeepThoughtBackgroundAndroidService)

    fun inject(app: DeepThoughtApplication)

    fun inject(mainActivity: MainActivity)

    fun inject(appInitializer: AndroidAppInitializer)

    fun inject(communicationManagerStarter: CommunicationManagerStarter)

    fun inject(networkConnectivityChangeBroadcastReceiver: NetworkConnectivityChangeBroadcastReceiver)

    fun inject(snackbarService: SnackbarService)

    fun inject(androidClipboardService: AndroidClipboardService)

    fun inject(extractArticleHandler: ExtractArticleHandler)

    fun inject(entriesListView: EntriesListView)

    fun inject(tagsListView: TagsListView)

    fun inject(referencesListView: ReferencesListView)

    fun inject(readLaterArticlesListView: ReadLaterArticlesListView)

    fun inject(entriesListDialogBase: EntriesListDialogBase)

    fun inject(tagEntriesListDialog: TagEntriesListDialog)

    fun inject(referenceEntriesListDialog: ReferenceEntriesListDialog)

    fun inject(addArticleSummaryExtractorDialog: AddArticleSummaryExtractorDialog)

    fun inject(articleSummaryExtractorIconsAdapter: ArticleSummaryExtractorIconsAdapter)

    fun inject(articleSummaryExtractorsDialog: ArticleSummaryExtractorsDialog)

    fun inject(articleSummaryExtractorsAdapter: ArticleSummaryExtractorsAdapter)

    fun inject(articleSummaryExtractorConfigDialog: ArticleSummaryExtractorConfigDialog)

    fun inject(articleSummaryActivity: ArticleSummaryActivity)

    fun inject(editEntryActivity: EditEntryActivity)

    fun inject(editHtmlTextDialog: EditHtmlTextDialog)

    fun inject(tagsOnEntryDialogFragment: TagsOnEntryDialogFragment)

    fun inject(editReferenceActivity: EditReferenceActivity)

    fun inject(editSeriesActivity: EditSeriesActivity)

}