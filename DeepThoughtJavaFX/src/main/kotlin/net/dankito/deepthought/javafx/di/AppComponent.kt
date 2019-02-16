package net.dankito.deepthought.javafx.di

import dagger.Component
import net.dankito.deepthought.di.BaseModule
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonDataModule
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.javafx.DeepThoughtJavaFXApplication
import net.dankito.deepthought.javafx.appstart.CommunicationManagerStarter
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.item.EditItemExtractionResultView
import net.dankito.deepthought.javafx.dialogs.item.EditItemViewBase
import net.dankito.deepthought.javafx.dialogs.item.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.item.controls.EditItemSourceField
import net.dankito.deepthought.javafx.dialogs.item.controls.EditItemTagsField
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.*
import net.dankito.deepthought.javafx.dialogs.pdf.ViewPdfDialog
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleListView
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.javafx.dialogs.source.controls.EditSourceSeriesField
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.javafx.ui.controls.EditEntityFilesField
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

    fun inject(mainMenuBar: MainMenuBar)

    fun inject(javaFXClipboardWatcher: JavaFXClipboardWatcher)

    fun inject(messagePopupPane: MessagePopupPane)

    fun inject(articleExtractorsMenuButton: ArticleExtractorsMenuButton)

    fun inject(dialogFragment: DialogFragment)

    fun inject(itemsListView: ItemsListView)

    fun inject(tagsListView: TagsListView)

    fun inject(sourcesListView: SourcesListView)

    fun inject(articleSummaryView: ArticleSummaryView)

    fun inject(readLaterArticleListView: ReadLaterArticleListView)

    fun inject(editItemViewBase: EditItemViewBase)

    fun inject(editItemExtractionResultView: EditItemExtractionResultView)

    fun inject(editReadLaterArticleView: EditReadLaterArticleView)

    fun inject(editEntityFilesField: EditEntityFilesField)

    fun inject(editItemSourceField: EditItemSourceField)

    fun inject(editItemTagsField: EditItemTagsField)

    fun inject(editSourceDialog: EditSourceDialog)

    fun inject(editSourceSeriesField: EditSourceSeriesField)

    fun inject(viewPdfDialog: ViewPdfDialog)

}