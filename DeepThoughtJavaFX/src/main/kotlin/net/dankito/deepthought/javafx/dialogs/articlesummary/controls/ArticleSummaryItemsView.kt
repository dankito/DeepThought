package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryController
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemsView(private val controller: ArticleSummaryController) : View() {

    override val root = listview<ArticleSummaryItem> {
        items = controller.items

        bindSelected(controller.itemModel)

        prefWidth = 800.0
        prefHeight = 400.0

        cellFormat {
            graphic = hbox {
                imageview(it.previewImageUrl) {
                    isPreserveRatio = true
                    fitHeight = 100.0
                    fitWidth = 120.0
                }

                vbox {
                    hboxConstraints {
                        hgrow = Priority.ALWAYS
                        marginLeftRight(6.0)
                    }

                    label(it.title)

                    label(it.summary) {
                        vgrow = Priority.ALWAYS

                        isWrapText = true
                    }
                }
            }
        }
    }
}