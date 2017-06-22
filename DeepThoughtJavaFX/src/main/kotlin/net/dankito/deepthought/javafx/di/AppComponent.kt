package net.dankito.deepthought.javafx.di

import dagger.Component
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.java.service.di.JavaCommonModule
import net.dankito.deepthought.javafx.DeepThoughtJavaFXApplication
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryViewBase
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntriesListView
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.TagsListView
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(JavaFXModule::class, JavaCommonModule::class, CommonModule::class, CommonDataModule::class, BaseModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
    }



    fun inject(app: DeepThoughtJavaFXApplication)

    fun inject(mainWindow: MainWindow)

    fun inject(mainWindowController: MainWindowController)

    fun inject(entriesListView: EntriesListView)

    fun inject(tagsListView: TagsListView)

    fun inject(articleSummaryView: ArticleSummaryView)

    fun inject(editEntryViewBase: EditEntryViewBase)

}