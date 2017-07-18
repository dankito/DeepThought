package net.dankito.deepthought.javafx.dialogs.readlaterarticle

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.controls.ReadLaterArticleListCellFragment
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReadLaterArticlePresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import javax.inject.Inject


class ReadLaterArticleView : View(), IReadLaterArticleView {


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticlePresenter


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticlePresenter(this, searchEngine, readLaterArticleService, entryPersister, router)

        presenter.getAndShowAllEntities()
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
                    selectedItem?.let { presenter.copyUrlToClipboard(it) }
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


    override fun showArticles(readLaterArticles: List<ReadLaterArticle>) {
        runLater {
            root.items.setAll(readLaterArticles)
        }
    }

}