package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.deepthought.javafx.appstart.CommunicationManagerStarter
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.dialogs.JavaFXDialogService
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.communication.JavaFXIsSynchronizationPermittedHandler
import net.dankito.deepthought.ui.IDialogService
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
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
    fun provideDialogService() : IDialogService {
        return JavaFXDialogService()
    }


    @Provides
    @Singleton
    fun provideIsSynchronizationPermittedHandler() : IsSynchronizationPermittedHandler {
        return JavaFXIsSynchronizationPermittedHandler()
    }

}