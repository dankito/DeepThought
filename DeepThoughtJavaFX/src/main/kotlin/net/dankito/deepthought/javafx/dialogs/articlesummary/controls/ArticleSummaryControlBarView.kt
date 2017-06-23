package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.JavaFXArticleSummaryPresenter
import net.dankito.deepthought.javafx.res.icons.IconPaths
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryControlBarView(private val presenter: JavaFXArticleSummaryPresenter, private val articleSummaryItemsView: ArticleSummaryItemsView) : View() {

    companion object {
        private const val ButtonsHeight = 40.0

        private const val TextButtonsWidth = 120.0

        private const val IconButtonsWidth = ButtonsHeight

        private const val ButtonsTopAndBottomMargin = 4.0

        private const val ButtonsLeftMargin = 12.0

        private const val BarHeight = ButtonsHeight + 2 * ButtonsTopAndBottomMargin
    }


    private val countItemsSelectedLabelText = SimpleStringProperty(String.format(messages["count.items.selected"], 0))

    private val areSelectedItemsActionButtonsDisabled = SimpleBooleanProperty(true)


    init {
        articleSummaryItemsView.checkedItems.addListener(MapChangeListener<ArticleSummaryItem, CheckBox> {
            areSelectedItemsActionButtonsDisabled.set(it.map.size == 0)

            countItemsSelectedLabelText.set(String.format(messages["count.items.selected"], it.map.size))
        })
    }


    override val root = anchorpane {
        minHeight = BarHeight
        maxHeight = BarHeight

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
                prefWidth = TextButtonsWidth
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { viewSelectedItems() }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }

            button(messages["read.selected.items.later"]) {
                prefWidth = TextButtonsWidth
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { saveSelectedItemsForLaterReading() }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }

            button(messages["save.selected.items"]) {
                prefWidth = TextButtonsWidth
                useMaxHeight = true
                disableProperty().bind(areSelectedItemsActionButtonsDisabled)

                action { saveSelectedItems() }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }
        }

        hbox {
            anchorpaneConstraints {
                topAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
            }

            alignment = Pos.CENTER_LEFT

            label(presenter.lastUpdateTime) {
                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                }
            }

            button {
                minHeight = ButtonsHeight
                maxHeight = ButtonsHeight
                minWidth = IconButtonsWidth
                maxWidth = IconButtonsWidth

                contentDisplay = ContentDisplay.GRAPHIC_ONLY
                graphic = ImageView(IconPaths.UpdateIconPath)

                action { presenter.extractArticlesSummary() }

                hboxConstraints {
                    marginLeft = 6.0
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }

            button {
                minHeight = ButtonsHeight
                maxHeight = ButtonsHeight
                minWidth = IconButtonsWidth
                maxWidth = IconButtonsWidth

                visibleProperty().bind(presenter.canLoadMoreItems)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                contentDisplay = ContentDisplay.GRAPHIC_ONLY
                graphic = ImageView(IconPaths.LoadNextItemsIconPath)

                action { presenter.loadMoreItems() }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
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