package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ContentDisplay
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.res.icons.IconPaths
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.messages.ArticleSummaryExtractorConfigChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ReadLaterArticleSearch
import net.engio.mbassy.listener.Handler
import tornadofx.*
import javax.inject.Inject


class ArticleExtractorsMenuButton(private val controller: MainWindowController) : View() {

    companion object {
        private const val ICON_SIZE = 38.0
    }


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    private val btnArticleExtractors: MenuButton

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)

        btnArticleExtractors = MenuButton()
        setupArticleExtractorsMenuButton()

        extractorsConfigManager.addInitializationListener { runLater {
            extractorsConfigManager.getConfigs().forEach { addArticleSummaryExtractor(it) }
        } }

        eventBus.register(eventBusListener)
    }

    override val root: Parent
        get() = btnArticleExtractors

    private fun setupArticleExtractorsMenuButton() {
        btnArticleExtractors.contentDisplay = ContentDisplay.GRAPHIC_ONLY
        btnArticleExtractors.maxHeight = 26.0
        btnArticleExtractors.prefWidth = 60.0
        btnArticleExtractors.isVisible = false

        btnArticleExtractors.items.clear() // remove automatically added 'Article 1' and 'Article 2'

        btnArticleExtractors.setGraphic(ImageView(IconPaths.NewspaperIconPath))

        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(root)

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
        synchronized(this) {
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


    inner class EventBusListener {

        @Handler
        fun articleSummaryExtractorChanged(changed: ArticleSummaryExtractorConfigChanged) {
            FXUtils.runOnUiThread { articleSummaryExtractorUpdated(changed.entity) }
        }
    }

}