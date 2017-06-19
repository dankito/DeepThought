package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemModel
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemListCellFragment : ListCellFragment<ArticleSummaryItem>() {

    val summaryItem = ArticleSummaryItemModel().bindTo(this)

    override val root = hbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty()) }
        }

        imageview(summaryItem.previewImageUrl) {
            isPreserveRatio = true
            fitHeight = 100.0
            fitWidth = 120.0
        }

        vbox {
            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            label(summaryItem.title)

            label(summaryItem.summary) {
                vgrow = Priority.ALWAYS

                isWrapText = true
            }
        }
    }

}