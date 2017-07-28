package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.javafx.appstart.CommunicationManagerStarter
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.dialogs.JavaFXDialogService
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.JavaFXClipboardService
import net.dankito.deepthought.javafx.service.communication.JavaFXDeviceRegistrationHandler
import net.dankito.deepthought.javafx.service.network.JavaFXNetworkConnectivityManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.localization.Localization
import net.dankito.utils.services.network.INetworkConnectivityManager
import net.dankito.utils.services.network.NetworkHelper
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import javax.inject.Singleton


@Module
class JavaFXModule(private val mainWindowController: MainWindowController) {

    @Provides
    @Singleton
    fun provideAppInitializer() : JavaFXAppInitializer {
        return JavaFXAppInitializer()
    }

    @Provides
    @Singleton
    fun provideCommunicationManagerStarter(searchEngine: ISearchEngine) : CommunicationManagerStarter {
        return CommunicationManagerStarter(searchEngine)
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
    fun provideDialogService(localization: Localization) : IDialogService {
        return JavaFXDialogService(localization)
    }


    @Provides
    @Singleton
    fun provideDeviceRegistrationHandler(dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService, localization: Localization) : IDeviceRegistrationHandler {
        return JavaFXDeviceRegistrationHandler(dataManager, initialSyncManager, dialogService, localization)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(networkHelper: NetworkHelper) : INetworkConnectivityManager {
        return JavaFXNetworkConnectivityManager(networkHelper)
    }

}