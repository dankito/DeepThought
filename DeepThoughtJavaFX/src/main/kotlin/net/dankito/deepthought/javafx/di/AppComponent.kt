package net.dankito.deepthought.javafx.di

import dagger.Component
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.java.service.di.JavaCommonModule
import net.dankito.deepthought.javafx.DeepThoughtJavaFXApplication
import net.dankito.deepthought.javafx.appstart.CommunicationManagerStarter
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryViewBase
import net.dankito.deepthought.javafx.dialogs.entry.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntriesListView
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.TagsListView
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleView
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(JavaFXModule::class, JavaCommonModule::class, CommonModule::class, CommonDataModule::class, BaseModule::class))
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
    }



    fun inject(app: DeepThoughtJavaFXApplication)

    fun inject(communicationManagerStarter: CommunicationManagerStarter)

    fun inject(appInitializer: JavaFXAppInitializer)

    fun inject(mainWindow: MainWindow)

    fun inject(mainWindowController: MainWindowController)

    fun inject(entriesListView: EntriesListView)

    fun inject(tagsListView: TagsListView)

    fun inject(articleSummaryView: ArticleSummaryView)

    fun inject(readLaterArticleView: ReadLaterArticleView)

    fun inject(editEntryViewBase: EditEntryViewBase)

    fun inject(editReadLaterArticleView: EditReadLaterArticleView)

}