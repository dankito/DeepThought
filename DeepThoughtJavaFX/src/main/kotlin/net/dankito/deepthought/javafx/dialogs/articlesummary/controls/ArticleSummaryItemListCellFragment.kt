package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.collections.ObservableMap
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemViewModel
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemListCellFragment : ListCellFragment<ArticleSummaryItem>() {

    val summaryItem = ArticleSummaryItemViewModel().bindTo(this)


    override val root = hbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }
        }

        alignment = Pos.CENTER
        minHeight = 100.0
        prefHeight = 100.0

        checkbox {
            prefWidth = 30.0

            selectedProperty().addListener { _, _, isSelected ->
                itemSelectionChanged(isSelected, item, this)
            }

            hboxConstraints {
                alignment = Pos.CENTER
            }
        }

        imageview(summaryItem.previewImageUrl) {
            isPreserveRatio = true
            fitHeight = 100.0
            fitWidth = 120.0
        }

        vbox {
            useMaxHeight = true
            prefHeight = Region.USE_COMPUTED_SIZE
            alignment = Pos.CENTER_LEFT

            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            label(summaryItem.title) {
                maxHeight = 20.0
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                vboxConstraints {
                    marginBottom = 6.0
                }
            }

            label(summaryItem.summary) {
                vgrow = Priority.ALWAYS

                isWrapText = true
            }
        }
    }

    private fun itemSelectionChanged(isSelected: Boolean, item: ArticleSummaryItem, checkBox: CheckBox) {
        // really bad code design
        (cell?.listView?.userData as? ObservableMap<ArticleSummaryItem, CheckBox>)?.let { checkedItems ->
            if (isSelected) {
                checkedItems.put(item, checkBox)
            } else {
                checkedItems.remove(item)
            }
        }
    }

}