package net.dankito.deepthought.javafx.di

import dagger.Component
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.java.service.di.JavaCommonModule
import net.dankito.deepthought.javafx.DeepThoughtJavaFXApplication
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(JavaFXModule::class, JavaCommonModule::class, CommonModule::class, CommonDataModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
    }



    fun inject(app: DeepThoughtJavaFXApplication)

    fun inject(mainWindowController: MainWindowController)

}