package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryController
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemsView(private val controller: ArticleSummaryController) : View() {

    override val root = listview<ArticleSummaryItem> {
        items = controller.items

        bindSelected(controller.itemModel)

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ArticleSummaryItemListCellFragment::class)

        onDoubleClick { controller.itemDoubleClicked(selectedItem) }
    }
}