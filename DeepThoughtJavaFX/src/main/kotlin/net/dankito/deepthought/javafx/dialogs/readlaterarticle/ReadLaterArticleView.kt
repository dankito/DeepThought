package net.dankito.deepthought.javafx.dialogs.readlaterarticle

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.controls.ReadLaterArticleListCellFragment
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReadLaterArticlePresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.service.data.ReadLaterArticleService
import tornadofx.*
import javax.inject.Inject


class ReadLaterArticleView : View(), IReadLaterArticleView {


    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticlePresenter

    private var extractionResultToArticlesToMap: Map<EntryExtractionResult, ReadLaterArticle> = mapOf()


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticlePresenter(this, readLaterArticleService, entryPersister, router)

        presenter.getAndShowAllEntities()
    }


    override fun onUndock() {
        presenter.cleanUp()

        super.onDock()
    }


    override val root = listview<EntryExtractionResult> {

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ReadLaterArticleListCellFragment::class)

        onDoubleClick {
            selectedItem?.let { presenter.showArticle(it) }
        }

        contextmenu {
            item(messages["context.menu.read.later.article.save"]) {
                action {
                    selectedItem?.let { presenter.saveAndDeleteReadLaterArticle(it, extractionResultToArticlesToMap[it]) }
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
                    selectedItem?.let { presenter.deleteReadLaterArticle(extractionResultToArticlesToMap[it]) }
                }
            }
        }

    }


    override fun showArticles(extractionResultToArticlesToMap: Map<EntryExtractionResult, ReadLaterArticle>) {
        runLater {
            this.extractionResultToArticlesToMap = extractionResultToArticlesToMap
            root.items.setAll(extractionResultToArticlesToMap.keys)
        }
    }

}