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
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.localization.Localization
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
    fun provideDialogService() : IDialogService {
        return JavaFXDialogService()
    }


    @Provides
    @Singleton
    fun provideDeviceRegistrationHandler(dialogService: IDialogService, localization: Localization) : IDeviceRegistrationHandler {
        return JavaFXDeviceRegistrationHandler(dialogService, localization)
    }

}