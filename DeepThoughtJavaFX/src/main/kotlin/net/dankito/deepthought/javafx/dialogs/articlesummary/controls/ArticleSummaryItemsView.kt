package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemViewModel
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.JavaFXArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemsView(private val presenter: JavaFXArticleSummaryPresenter) : View() {

    val checkedItems: ObservableSet<ArticleSummaryItem> = FXCollections.observableSet(LinkedHashSet())


    private val itemModel = ArticleSummaryItemViewModel()

    private val items = FXCollections.observableArrayList<ArticleSummaryItem>()


    override val root = listview<ArticleSummaryItem> {
        items = this@ArticleSummaryItemsView.items

        userData = checkedItems // bad code design

        bindSelected(itemModel)

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ArticleSummaryItemListCellFragment::class)

        contextmenu {
            item(messages["context.menu.article.summary.item.save"]) {
                action {
                    selectedItem?.let { presenter.getAndSaveArticle(it) }
                }
            }

            item(messages["context.menu.article.summary.item.save.for.later.reading"]) {
                action {
                    selectedItem?.let { presenter.getAndSaveArticleForLaterReading(it) }
                }
            }

            separator()

            item(messages["context.menu.article.summary.item.copy.url.to.clipboard"]) {
                isDisable = true
            }
        }

        onDoubleClick {
            selectedItem?.let { presenter.getAndShowArticle(it) }
        }
    }


    fun clearSelectedItems() {
        checkedItems.clear()
    }


    fun showArticlesOnUiThread(articles: List<ArticleSummaryItem>, indexToScrollTo: Int) {
        items.setAll(articles)

        root.scrollTo(indexToScrollTo)
    }

}