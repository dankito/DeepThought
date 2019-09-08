package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemViewModel
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*


class ArticleSummaryItemListCellFragment : ListCellFragment<ArticleSummaryItem>() {

    val summaryItem = ArticleSummaryItemViewModel().bindTo(this)

    var checkedItems: ObservableSet<ArticleSummaryItem>? = null

    private val tooltipText = SimpleStringProperty("")

    private lateinit var checkBoxIsItemSelected: CheckBox


    init {
        cellProperty.addListener { _, _, newValue ->
            if(newValue?.listView?.userData is ObservableSet<*>) {
                checkedItems = newValue?.listView?.userData as? ObservableSet<ArticleSummaryItem>

                setCheckBoxState()

                checkedItems?.addListener(SetChangeListener<ArticleSummaryItem> {
                    setCheckBoxState()
                })
            }
        }

        itemProperty.addListener { _, _, newValue ->
            setCheckBoxState()

            tooltipText.set(if (newValue == null) "" else "${summaryItem.title.value}${System.lineSeparator()}${summaryItem.summary.value}")
        }
    }

    private fun setCheckBoxState() {
        checkBoxIsItemSelected.isSelected = checkedItems?.contains(item) ?: false
    }

    override val root = hbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }
        }

        alignment = Pos.CENTER
        minHeight = 100.0
        prefHeight = 100.0

        checkBoxIsItemSelected = checkbox {
            prefWidth = 30.0

            selectedProperty().addListener { _, _, isSelected ->
                item?.let { itemSelectionChanged(isSelected, it) }
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
                ensureOnlyUsesSpaceIfVisible()

                vboxConstraints {
                    marginBottom = 6.0
                }
            }

            label(summaryItem.summary) {
                vgrow = Priority.ALWAYS

                isWrapText = true
            }
        }

        tooltip {
            textProperty().bind(tooltipText)

            isWrapText = true

            maxWidth = 600.0
        }
    }

    private fun itemSelectionChanged(isSelected: Boolean, item: ArticleSummaryItem) {
        checkedItems?.let { checkedItems ->
            if (isSelected) {
                checkedItems.add(item)
            }
            else {
                checkedItems.remove(item)
            }
        }
    }

}