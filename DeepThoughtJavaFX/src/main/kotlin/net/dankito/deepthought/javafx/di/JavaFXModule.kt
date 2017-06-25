package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.deepthought.javafx.dialogs.JavaFXDialogService
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.communication.JavaFXIsSynchronizationPermittedHandler
import net.dankito.deepthought.ui.IDialogService
import net.dankito.deepthought.ui.IRouter
import javax.inject.Singleton


@Module
class JavaFXModule(private val mainWindowController: MainWindowController) {

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