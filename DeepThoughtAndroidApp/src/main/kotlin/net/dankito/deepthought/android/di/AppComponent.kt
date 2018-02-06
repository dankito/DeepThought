package net.dankito.deepthought.android.di

import dagger.Component
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.activities.*
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorIconsAdapter
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.androidservice.DeepThoughtBackgroundAndroidService
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.appstart.CommunicationManagerStarter
import net.dankito.deepthought.android.dialogs.*
import net.dankito.deepthought.android.fragments.ItemsListView
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.SourcesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.service.AndroidClipboardService
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.SnackbarService
import net.dankito.deepthought.android.service.network.NetworkConnectivityChangeBroadcastReceiver
import net.dankito.deepthought.android.views.EditEntityFilesField
import net.dankito.deepthought.android.views.EditEntityTagsField
import net.dankito.deepthought.android.views.EditItemSourceField
import net.dankito.deepthought.android.views.EditSourceSeriesField
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

    fun inject(baseActivity: BaseActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(appInitializer: AndroidAppInitializer)

    fun inject(communicationManagerStarter: CommunicationManagerStarter)

    fun inject(networkConnectivityChangeBroadcastReceiver: NetworkConnectivityChangeBroadcastReceiver)

    fun inject(snackbarService: SnackbarService)

    fun inject(androidClipboardService: AndroidClipboardService)

    fun inject(extractArticleHandler: ExtractArticleHandler)

    fun inject(itemsListView: ItemsListView)

    fun inject(tagsListView: TagsListView)

    fun inject(sourcesListView: SourcesListView)

    fun inject(readLaterArticlesListView: ReadLaterArticlesListView)

    fun inject(itemsListDialogBase: ItemsListDialogBase)

    fun inject(tagItemsListDialog: TagItemsListDialog)

    fun inject(sourceItemsListDialog: SourceItemsListDialog)

    fun inject(addArticleSummaryExtractorDialog: AddArticleSummaryExtractorDialog)

    fun inject(articleSummaryExtractorIconsAdapter: ArticleSummaryExtractorIconsAdapter)

    fun inject(articleSummaryExtractorsDialog: ArticleSummaryExtractorsDialog)

    fun inject(articleSummaryExtractorsAdapter: ArticleSummaryExtractorsAdapter)

    fun inject(articleSummaryExtractorConfigDialog: ArticleSummaryExtractorConfigDialog)

    fun inject(articleSummaryActivity: ArticleSummaryActivity)

    fun inject(editItemActivity: EditItemActivity)

    fun inject(editEntityTagsField: EditEntityTagsField)

    fun inject(editItemSourceField: EditItemSourceField)

    fun inject(editSourceSeriesField: EditSourceSeriesField)

    fun inject(editEntityFilesField: EditEntityFilesField)

    fun inject(editSourceActivity: EditSourceActivity)

    fun inject(editSeriesActivity: EditSeriesActivity)

    fun inject(viewPdfActivity: ViewPdfActivity)

}