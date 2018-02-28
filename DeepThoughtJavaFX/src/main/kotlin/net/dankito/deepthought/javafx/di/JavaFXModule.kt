package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import javafx.stage.Stage
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.javafx.appstart.CommunicationManagerStarter
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.dialogs.JavaFXDialogService
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.JavaFXApplicationsService
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardService
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.javafx.service.communication.JavaFXDeviceRegistrationHandler
import net.dankito.deepthought.javafx.service.import_export.DataImporterExporterManager
import net.dankito.deepthought.javafx.service.settings.JavaFXLocalSettingsStore
import net.dankito.deepthought.model.AllCalculatedTags
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.permissions.IPermissionsService
import net.dankito.deepthought.service.permissions.JavaPermissionsService
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.util.IThreadPool
import net.dankito.util.UrlUtil
import net.dankito.util.localization.Localization
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.network.JavaNetworkConnectivityManager
import net.dankito.util.network.NetworkHelper
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.util.ui.IApplicationsService
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.web.IWebClient
import net.dankito.utils.ui.IClipboardService
import javax.inject.Singleton


@Module
class JavaFXModule(private val primaryStage: Stage, private val flavorInstanceProvider: JavaFXInstanceProvider, private val mainWindowController: MainWindowController) {

    @Provides
    @Singleton
    fun provideAppInitializer() : JavaFXAppInitializer {
        return JavaFXAppInitializer()
    }

    @Provides
    @Singleton
    fun provideLocalSettingsStore() : ILocalSettingsStore {
        return JavaFXLocalSettingsStore()
    }

    @Provides
    @Singleton
    fun provideCommunicationManagerStarter(dataManager: DataManager) : CommunicationManagerStarter {
        return CommunicationManagerStarter(dataManager)
    }


    @Provides
    @Singleton
    fun provideRouter() : IRouter {
        return JavaFXRouter(mainWindowController)
    }


    @Provides
    @Singleton
    fun provideClipboardService() : IClipboardService {
        return JavaFXClipboardService()
    }

    @Provides
    @Singleton
    fun provideJavaFXClipboardWatcher(urlUtil: UrlUtil) : JavaFXClipboardWatcher {
        return JavaFXClipboardWatcher(primaryStage, urlUtil)
    }

    @Provides
    @Singleton
    fun providePermissionsService() : IPermissionsService {
        return JavaPermissionsService()
    }

    @Provides
    @Singleton
    fun provideApplicationsService(threadPool: IThreadPool) : IApplicationsService {
        return JavaFXApplicationsService(threadPool)
    }

    @Provides
    @Singleton
    fun provideDialogService(localization: Localization) : IDialogService {
        return JavaFXDialogService(localization, true, "deepthought@dankito.net", "Bug in DeepThought")
    }


    @Provides
    @Singleton
    fun provideDeviceRegistrationHandler(dataManager: DataManager, entityManager: IEntityManager, networkSettings: NetworkSettings, initialSyncManager: DeepThoughtInitialSyncManager,
                                         dialogService: IDialogService, localization: Localization) : IDeviceRegistrationHandler {
        return JavaFXDeviceRegistrationHandler(dataManager.deepThought, entityManager, networkSettings, initialSyncManager, dialogService, localization)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(networkHelper: NetworkHelper) : INetworkConnectivityManager {
        return JavaNetworkConnectivityManager(networkHelper)
    }


    @Provides
    @Singleton
    fun provideArticleSummaryPresenter(itemPersister: ItemPersister, readLaterArticleService: ReadLaterArticleService, articleExtractorManager: ArticleExtractorManager,
                                       router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService) : ArticleSummaryPresenter {
        return ArticleSummaryPresenter(itemPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)
    }

    @Provides
    @Singleton
    fun provideArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine) : ArticleExtractorManager {
        return flavorInstanceProvider.provideArticleExtractorManager(seriesService, searchEngine)
    }

    @Provides
    @Singleton
    fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return flavorInstanceProvider.provideImplementedArticleSummaryExtractorsManager(webClient)
    }

    @Provides
    @Singleton
    fun provideAllCalculatedTags(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization) : AllCalculatedTags {
        return AllCalculatedTags(searchEngine, eventBus, entityChangedNotifier, localization)
    }


    // TODO: move to CommonModule again as soon as importing/exporting is implemented in Android
    @Provides
    @Singleton
    fun provideDataImporterExporterManager(searchEngine: ISearchEngine, itemPersister: ItemPersister, tagService: TagService,
                                           sourcePersister: SourcePersister, seriesPersister: SeriesPersister, threadPool: IThreadPool)
            : DataImporterExporterManager {
        return DataImporterExporterManager(searchEngine, itemPersister, tagService, sourcePersister, seriesPersister, threadPool)
    }

}