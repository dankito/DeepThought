package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.scene.control.CheckBox
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.JavaFXArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemsView(private val presenter: JavaFXArticleSummaryPresenter) : View() {

    val checkedItems: ObservableMap<ArticleSummaryItem, CheckBox> = FXCollections.observableHashMap()


    override val root = listview<ArticleSummaryItem> {
        items = presenter.items

        userData = checkedItems // bad code design

        bindSelected(presenter.itemModel)

        prefWidth = 800.0
        prefHeight = 400.0

        cellFragment(ArticleSummaryItemListCellFragment::class)

        onDoubleClick {
            selectedItem?.let { presenter.getAndShowArticle(it) }
        }
    }


    fun clearSelectedItems() {
        ArrayList(checkedItems.values).forEach { it.isSelected = false }
    }

}