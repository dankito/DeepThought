package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntriesListView
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.TagsListView
import net.dankito.deepthought.javafx.res.icons.IconPaths
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardContent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ReadLaterArticleSearch
import tornadofx.*
import javax.inject.Inject


class MainWindow : View() {

    companion object {
        private const val ICON_SIZE = 38.0
    }


    val controller: MainWindowController by inject()


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var router: IRouter


    override val root: BorderPane by fxml()

    private var tbpnOverview: TabPane by singleAssign()

    private var tabTags: Tab by singleAssign()

    private var splpnContent: SplitPane by singleAssign()

    private var contentPane: VBox by singleAssign()

    private val mnitmFileClipboard: Menu by fxid()

    private val btnArticleExtractors: MenuButton by fxid()

    val tagsListView: TagsListView by inject()

    val entriesListView: EntriesListView by inject()

    private val clipboardWatcher: JavaFXClipboardWatcher


    init {
        AppComponent.component.inject(this)

        setupUI()

        clipboardWatcher = JavaFXClipboardWatcher(primaryStage) { clipboardContentChangedExternally(it) }
    }

    private fun setupUI() {
        splpnContent = splitpane {
            tbpnOverview = tabpane {
                prefWidth = 300.0
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tabTags = tab(messages["tags.tab.label"]) {
                    prefWidth = 300.0

                    add(tagsListView.root)
                }
            }

            contentPane = vbox {

            }
        }

        root.center = splpnContent

        contentPane.add(entriesListView.root)
        VBox.setVgrow(entriesListView.root, Priority.ALWAYS)

        splpnContent.setDividerPosition(0, 0.2)

        setupButtonArticleExtractors()
    }

    private fun setupButtonArticleExtractors() {
        btnArticleExtractors.items.clear() // remove automatically added 'Article 1' and 'Article 2'

        btnArticleExtractors.setGraphic(ImageView(IconPaths.NewspaperIconPath))

        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnArticleExtractors)

        dataManager.addInitializationListener {
            searchEngine.searchReadLaterArticles(ReadLaterArticleSearch {
                if(it.isNotEmpty()) {
                    runLater { addShowReadLaterArticlesMenuItem() }
                }
            })
        }
    }


    private fun addShowReadLaterArticlesMenuItem() {
        val showReadLaterArticlesItem = MenuItem(messages["article.extractors.item.show.read.later.articles"])
        showReadLaterArticlesItem.action { controller.showReadLaterArticlesView() }

        addMenuButtonArticleExtractorsMenuItem(showReadLaterArticlesItem, 0)

        addMenuButtonArticleExtractorsMenuItem(SeparatorMenuItem(), 1)

        btnArticleExtractors.isVisible = true
    }

    fun addArticleSummaryExtractor(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        val extractorItem = MenuItem(articleSummaryExtractorConfig.name)
        extractorItem.tag = articleSummaryExtractorConfig
        extractorItem.setOnAction { controller.showArticlesSummaryView(articleSummaryExtractorConfig) }

        val graphicPane = hbox {
            minWidth = ICON_SIZE
            maxWidth = ICON_SIZE
            minHeight = ICON_SIZE
            maxHeight = ICON_SIZE
            alignment = Pos.CENTER
        }
        extractorItem.graphic = graphicPane

        articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
            createOnlineArticleContentExtractorIcon(graphicPane, iconUrl)
        }

        addMenuButtonArticleExtractorsMenuItem(extractorItem)
        btnArticleExtractors.isVisible = true
    }

    private fun createOnlineArticleContentExtractorIcon(graphicPane: HBox, iconUrl: String) {
        val iconView = ImageView(iconUrl)
        iconView.isPreserveRatio = true
        iconView.fitHeight = ICON_SIZE
        iconView.fitWidth = ICON_SIZE

        graphicPane.clear()
        graphicPane.add(iconView)
    }

    private fun addMenuButtonArticleExtractorsMenuItem(item: MenuItem, index: Int = -1) {
        synchronized(btnArticleExtractors) {
            if(index >= 0) {
                btnArticleExtractors.items.add(index, item)
            }
            else {
                btnArticleExtractors.items.add(item)
            }
        }
    }

    fun articleSummaryExtractorUpdated(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        btnArticleExtractors.items.forEach { menuItem ->
            if(menuItem.tag == articleSummaryExtractorConfig) {
                menuItem.text = articleSummaryExtractorConfig.name

                articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
                    createOnlineArticleContentExtractorIcon(menuItem.graphic as HBox, iconUrl)
                }

                return@forEach
            }
        }
    }


    private fun clipboardContentChangedExternally(clipboardContent: JavaFXClipboardContent) {
        mnitmFileClipboard.isDisable = !clipboardContent.hasUrl()

        mnitmFileClipboard.items.clear()

        if(clipboardContent.hasUrl()) {
            val extractContentFromUrlMenuItem = MenuItem(messages["main.window.menu.view.extract.entry.from.url"])
            extractContentFromUrlMenuItem.action { extractEntryFromClipboardContent(clipboardContent) }
            mnitmFileClipboard.items.add(extractContentFromUrlMenuItem)
        }
    }

    private fun extractEntryFromClipboardContent(clipboardContent: JavaFXClipboardContent) {
        articleExtractorManager.extractArticleAndAddDefaultDataAsync(clipboardContent.url) {
            it.result?.let { router.showEditEntryView(it) }
//            it.error?.let { showErrorMessage(it, clipboardContent.url) }
        }
    }


    fun handleMenuItemFileCloseAction(event: ActionEvent) {
        primaryStage.close()
    }


    @FXML
    fun handleMenuItemToolsBackupsAction(event: Event) {

    }

    @FXML
    fun handleMainMenuWindowShowing(event: Event) {

    }

    protected var checkMenuItemViewShowQuickEditEntrySelectedChangeListener: ChangeListener<Boolean> = object : ChangeListener<Boolean> {
        override fun changed(observable: ObservableValue<out Boolean>, oldValue: Boolean?, newValue: Boolean?) {

        }
    }

}