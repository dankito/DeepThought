package net.dankito.deepthought.javafx.dialogs.readlaterarticle

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntitiesListView
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.controls.ReadLaterArticleListCellFragment
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.search.ISearchEngine
import net.dankito.synchronization.search.Search
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


class ReadLaterArticleListView : EntitiesListView(), IReadLaterArticleView {


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var itemPersister: ItemPersister

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticleListPresenter


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticleListPresenter(this, searchEngine, readLaterArticleService, itemPersister, clipboardService, router)

        searchEntities(Search.EmptySearchTerm)
    }


    override fun onUndock() {
        presenter.cleanUp()

        super.onDock()
    }


    override val root = listview<ReadLaterArticle> {

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ReadLaterArticleListCellFragment::class)

        onDoubleClick {
            selectedItem?.let { presenter.showArticle(it) }
        }

        contextmenu {
            item(messages["context.menu.read.later.article.save"]) {
                action {
                    selectedItem?.let { presenter.saveAndDeleteReadLaterArticle(it) }
                }
            }

            item(messages["context.menu.read.later.article.copy.url.to.clipboard"]) {
                isDisable = true

                action {
                    selectedItem?.let { presenter.copySourceUrlToClipboard(it) }
                }
            }

            separator()

            item(messages["context.menu.read.later.article.delete"]) {
                action {
                    selectedItem?.let { presenter.deleteReadLaterArticle(it) }
                }
            }
        }

    }


    override fun searchEntities(query: String) {
        presenter.getReadLaterArticles(query)
    }

    override fun showEntities(entities: List<ReadLaterArticle>) {
        runLater {
            root.items.setAll(entities)
        }
    }

}