package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.appstart.CommunicationManagerStarter
import net.dankito.deepthought.android.dialogs.AndroidDialogService
import net.dankito.deepthought.android.routing.AndroidRouter
import net.dankito.deepthought.android.service.AndroidApplicationsService
import net.dankito.deepthought.android.service.AndroidClipboardService
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.SnackbarService
import net.dankito.deepthought.android.service.clipboard.AndroidClipboardWatcher
import net.dankito.deepthought.android.service.communication.AndroidDeviceRegistrationHandler
import net.dankito.deepthought.android.service.permissions.AndroidPermissionsService
import net.dankito.deepthought.android.service.settings.AndroidLocalSettingsStore
import net.dankito.deepthought.android.ui.UiStatePersister
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.AllCalculatedTags
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.permissions.IPermissionsService
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.android.network.AndroidNetworkConnectivityManager
import net.dankito.utils.android.ui.activities.AppLifeCycleListener
import net.dankito.utils.io.IFileStorageService
import net.dankito.utils.localization.Localization
import net.dankito.utils.network.INetworkConnectivityManager
import net.dankito.utils.network.NetworkHelper
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.ui.IApplicationsService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import net.dankito.utils.windowregistry.window.WindowRegistry
import javax.inject.Singleton


@Module
class ActivitiesModule(private val applicationContext: Context) {

    @Provides
    @Singleton
    fun provideApplicationContext() : Context {
        return applicationContext
    }


    @Provides
    @Singleton
    fun provideAppInitializer() : AndroidAppInitializer {
        return AndroidAppInitializer()
    }


    @Provides
    @Singleton
    fun provideLocalSettingsStore(context: Context) : ILocalSettingsStore {
        return AndroidLocalSettingsStore(context)
    }

    @Provides
    @Singleton
    fun provideAppLifeCycleListener() : AppLifeCycleListener {
        return AppLifeCycleListener()
    }

    @Provides
    @Singleton
    fun provideWindowRegistryActivityParameterHolder() : net.dankito.utils.android.ui.activities.ActivityParameterHolder {
        return net.dankito.utils.android.ui.activities.ActivityParameterHolder()
    }

    @Provides
    @Singleton
    fun provideUiStatePersister(fileStorageService: IFileStorageService, serializer: ISerializer) : UiStatePersister {
        return UiStatePersister(fileStorageService, serializer)
    }

    @Provides
    @Singleton
    fun provideExtractArticleHandler() : ExtractArticleHandler {
        return ExtractArticleHandler()
    }

    @Provides
    @Singleton
    fun provideSnackbarService() : SnackbarService {
        return SnackbarService()
    }

    @Provides
    @Singleton
    fun provideClipboardService() : IClipboardService {
        return AndroidClipboardService()
    }

    @Provides
    @Singleton
    fun provideAndroidClipboardWatcher(dataManager: DataManager) : AndroidClipboardWatcher {
        return AndroidClipboardWatcher(dataManager)
    }

    @Provides
    @Singleton
    fun providePermissionsService(context: Context, windowRegistry: WindowRegistry) : IPermissionsService {
        return AndroidPermissionsService(context, windowRegistry)
    }

    @Provides
    @Singleton
    fun provideApplicationsService(context: Context, fileManager: FileManager) : IApplicationsService {
        return AndroidApplicationsService(context, fileManager)
    }

    @Provides
    @Singleton
    fun provideDialogService(windowRegistry: WindowRegistry, localization: Localization) : IDialogService {
        return AndroidDialogService(windowRegistry, localization)
    }


    @Provides
    @Singleton
    fun provideRouter(context: Context, windowRegistry: WindowRegistry, parameterHolder: net.dankito.utils.android.ui.activities.ActivityParameterHolder, dataManager: DataManager) : IRouter {
        return AndroidRouter(context, windowRegistry, parameterHolder, dataManager)
    }

    @Provides
    @Singleton
    fun provideAllCalculatedTags(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization) : AllCalculatedTags {
        return AllCalculatedTags(searchEngine, eventBus, entityChangedNotifier, localization)
    }

    @Provides
    @Singleton
    fun provideArticleSummaryPresenter(itemPersister: ItemPersister, readLaterArticleService: ReadLaterArticleService, articleExtractorManager: ArticleExtractorManager,
                                       router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService) : ArticleSummaryPresenter {
        return ArticleSummaryPresenter(itemPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)
    }


    @Provides
    @Singleton
    fun provideDeviceRegistrationHandler(context: Context, dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService,
                                         localization: Localization, snackbarService: SnackbarService) : IDeviceRegistrationHandler {
        return AndroidDeviceRegistrationHandler(context, dataManager, initialSyncManager, dialogService, localization, snackbarService)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(networkHelper: NetworkHelper) : INetworkConnectivityManager {
        return AndroidNetworkConnectivityManager(networkHelper)
    }

    @Provides
    @Singleton
    fun provideCommunicationManagerStarter(dataManager: DataManager) : CommunicationManagerStarter {
        return CommunicationManagerStarter(dataManager)
    }

}