package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryPresenterJavaFX
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemsView(private val presenter: ArticleSummaryPresenterJavaFX) : View() {

    override val root = listview<ArticleSummaryItem> {
        items = presenter.items

        bindSelected(presenter.itemModel)

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ArticleSummaryItemListCellFragment::class)

        onDoubleClick { presenter.itemDoubleClicked(selectedItem) }
    }
}