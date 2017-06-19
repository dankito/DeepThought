package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntryView
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import tornadofx.*


class MainWindow : View() {

    companion object {
        private const val ICON_SIZE = 38.0
    }


    val controller: MainWindowController by inject()


    override val root: BorderPane by fxml()

    val tbpnOverview: TabPane by fxid()

    val tabTags: Tab by fxid()

    val btnOnlineArticleExtractors: MenuButton by fxid()

    val entryView: EntryView by inject()


    init {
        root.center = entryView.root

        btnOnlineArticleExtractors.items.clear() // remove automatically added 'Article 1' and 'Article 2'
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnOnlineArticleExtractors)
    }


    fun addArticleSummaryExtractor(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        val extractorItem = MenuItem(articleSummaryExtractorConfig.name)
        extractorItem.tag = articleSummaryExtractorConfig
        extractorItem.setOnAction { controller.showArticlesSummaryView(articleSummaryExtractorConfig) }

        val graphicPane = hbox {
            prefWidth = ICON_SIZE
            maxWidth = ICON_SIZE
            prefHeight = ICON_SIZE
            maxHeight = ICON_SIZE
            alignment = Pos.CENTER
        }
        extractorItem.graphic = graphicPane

        articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
            createOnlineArticleContentExtractorIcon(graphicPane, iconUrl)
        }

        btnOnlineArticleExtractors.items.add(extractorItem)
        btnOnlineArticleExtractors.isVisible = true
    }

    private fun createOnlineArticleContentExtractorIcon(graphicPane: HBox, iconUrl: String) {
        val iconView = ImageView(iconUrl)
        iconView.isPreserveRatio = true
        iconView.fitHeight = ICON_SIZE
        iconView.fitWidth = ICON_SIZE

        graphicPane.clear()
        graphicPane.add(iconView)
    }

    fun articleSummaryExtractorUpdated(articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) {
        btnOnlineArticleExtractors.items.forEach { menuItem ->
            if(menuItem.tag == articleSummaryExtractorConfig) {
                menuItem.text = articleSummaryExtractorConfig.name

                articleSummaryExtractorConfig.iconUrl?.let { iconUrl ->
                    createOnlineArticleContentExtractorIcon(menuItem.graphic as HBox, iconUrl)
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