package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.ui.IRouter
import javax.inject.Singleton


@Module
class JavaFXModule(private val mainWindowController: MainWindowController) {

    @Provides
    @Singleton
    fun provideRouter() : IRouter {
        return JavaFXRouter(mainWindowController)
    }

}