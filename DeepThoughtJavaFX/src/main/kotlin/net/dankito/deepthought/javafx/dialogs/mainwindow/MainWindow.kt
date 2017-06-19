package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import tornadofx.*


class MainWindow : View() {

    val controller: MainWindowController by inject()


    override val root: BorderPane by fxml()

    val tbpnOverview: TabPane by fxid()

    val tabTags: Tab by fxid()

    val btnOnlineArticleExtractors: MenuButton by fxid()


    init {
        btnOnlineArticleExtractors.items.clear() // remove automatically added 'Article 1' and 'Article 2'
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnOnlineArticleExtractors)
    }


    fun addArticleSummaryExtractor(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        val articleContentExtractorMenuItem = MenuItem(articleSummaryExtractorConfig.name)
        articleContentExtractorMenuItem.tag = articleSummaryExtractorConfig
        articleContentExtractorMenuItem.setOnAction { find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractor to articleSummaryExtractorConfig))
                .openWindow() }

        articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
            articleContentExtractorMenuItem.graphic = createOnlineArticleContentExtractorIcon(iconUrl)
        }

        btnOnlineArticleExtractors.items.add(articleContentExtractorMenuItem)
        btnOnlineArticleExtractors.isVisible = true
    }

    private fun createOnlineArticleContentExtractorIcon(iconUrl: String): Node {
        val iconView = ImageView(iconUrl)
        iconView.isPreserveRatio = true
        iconView.fitHeight = 38.0

        val graphicsPane = HBox(iconView)
        graphicsPane.prefWidth = 38.0
        graphicsPane.maxWidth = 38.0
        graphicsPane.maxHeight = 38.0
        graphicsPane.alignment = Pos.CENTER

        return graphicsPane
    }

    fun articleSummaryExtractorUpdated(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        btnOnlineArticleExtractors.items.forEach { menuItem ->
            if(menuItem.tag == articleSummaryExtractorConfig) {
                menuItem.text = articleSummaryExtractorConfig.name

                articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
                    menuItem.graphic = createOnlineArticleContentExtractorIcon(iconUrl)
                }

                return@forEach
            }
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