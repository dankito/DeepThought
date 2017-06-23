package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryPresenterJavaFX
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryControlBarView(private val presenter: ArticleSummaryPresenterJavaFX, private val articleSummaryItemsView: ArticleSummaryItemsView) : View() {

    private val areSelectedItemsActionButtonsDisabled = SimpleBooleanProperty(true)

    private val countItemsSelectedLabelText = SimpleStringProperty(String.format(messages["count.items.selected"], 0))


    init {
        articleSummaryItemsView.checkedItems.addListener(MapChangeListener<ArticleSummaryItem, CheckBox> {
            areSelectedItemsActionButtonsDisabled.set(it.map.size == 0)

            countItemsSelectedLabelText.set(String.format(messages["count.items.selected"], it.map.size))
        })
    }


    override val root = anchorpane {
        minHeight = 48.0
        maxHeight = 48.0

        hbox {
            anchorpaneConstraints {
                topAnchor = 0.0
                leftAnchor = 0.0
                bottomAnchor = 0.0
            }

            alignment = Pos.CENTER_LEFT

            label(countItemsSelectedLabelText) {
                hboxConstraints { marginLeft = 6.0 }
            }

            button(messages["view.selected.items"]) {
                prefWidth = 120.0
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { viewSelectedItems() }

                hboxConstraints {
                    marginLeft = 12.0
                    marginTopBottom(4.0)
                }
            }

            button(messages["read.selected.items.later"]) {
                prefWidth = 120.0
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { saveSelectedItemsForLaterReading() }

                hboxConstraints {
                    marginLeft = 12.0
                    marginTopBottom(4.0)
                }
            }

            button(messages["save.selected.items"]) {
                prefWidth = 120.0
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { saveSelectedItems() }

                hboxConstraints {
                    marginLeft = 12.0
                    marginTopBottom(4.0)
                }
            }
        }
    }


    private fun viewSelectedItems() {
        performActionOnSelectedItemsAndClearSelection { item ->
            presenter.getAndShowArticle(item) {
                // TODO
            }
        }
    }

    private fun saveSelectedItemsForLaterReading() {
        performActionOnSelectedItemsAndClearSelection { item ->
            presenter.getAndSaveArticleForLaterReading(item) {
                // TODO
            }
        }
    }

    private fun saveSelectedItems() {
        performActionOnSelectedItemsAndClearSelection { item ->
            presenter.getAndSaveArticle(item) {
                // TODO
            }
        }
    }


    private fun performActionOnSelectedItemsAndClearSelection(action: (ArticleSummaryItem) -> Unit) {
        getSelectedItems().forEach { action(it) }

        articleSummaryItemsView.clearSelectedItems()
    }

    private fun getSelectedItems(): List<ArticleSummaryItem> {
        return articleSummaryItemsView.checkedItems.keys.toList()
    }

}