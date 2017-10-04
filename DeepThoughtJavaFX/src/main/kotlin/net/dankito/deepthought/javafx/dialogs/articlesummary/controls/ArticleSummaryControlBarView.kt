package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.SetChangeListener
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.JavaFXArticleSummaryPresenter
import net.dankito.deepthought.javafx.res.icons.IconPaths
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*
import java.text.DateFormat
import java.util.*


class ArticleSummaryControlBarView(private val presenter: JavaFXArticleSummaryPresenter, private val articleSummaryItemsView: ArticleSummaryItemsView,
                                   private val articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) : View() {

    companion object {
        private const val ButtonsHeight = 40.0

        private const val TextButtonsWidth = 120.0

        private const val IconButtonsWidth = ButtonsHeight

        private const val ButtonsTopAndBottomMargin = 4.0

        private const val ButtonsLeftMargin = 12.0

        private const val BarHeight = ButtonsHeight + 2 * ButtonsTopAndBottomMargin

        private val LastUpdateTimeDateFormat = DateFormat.getDateTimeInstance()
    }


    val canLoadMoreItems = SimpleBooleanProperty(false)

    val lastUpdateTime = SimpleStringProperty("")


    private val countItemsSelectedLabelText = SimpleStringProperty(String.format(messages["count.items.selected"], 0))

    private val areSelectedItemsActionButtonsDisabled = SimpleBooleanProperty(true)


    init {
        articleSummaryItemsView.checkedItems.addListener(SetChangeListener<ArticleSummaryItem> {
            areSelectedItemsActionButtonsDisabled.set(it.set.size == 0)

            countItemsSelectedLabelText.set(String.format(messages["count.items.selected"], it.set.size))
        })

        presenter.extractArticlesSummary(articleSummaryExtractorConfig) { articleSummaryReceived(it) }
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

            button(messages["save.selected.items.for.later.reading"]) {
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

            label(lastUpdateTime) {
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

                action { presenter.extractArticlesSummary(articleSummaryExtractorConfig) { articleSummaryReceived(it) } }

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

                visibleProperty().bind(canLoadMoreItems)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                contentDisplay = ContentDisplay.GRAPHIC_ONLY
                graphic = ImageView(IconPaths.LoadNextItemsIconPath)

                action { presenter.loadMoreItems(articleSummaryExtractorConfig) { articleSummaryReceived(it) } }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }
        }
    }


    private fun viewSelectedItems() {
        presenter.getAndShowArticlesAsync(getSelectedItems()) {
            runLater { articleSummaryItemsView.clearSelectedItems() }
        }
    }

    private fun saveSelectedItemsForLaterReading() {
        presenter.getAndSaveArticlesForLaterReadingAsync(getSelectedItems()) {
            runLater { articleSummaryItemsView.clearSelectedItems() }
        }
    }

    private fun saveSelectedItems() {
        presenter.getAndSaveArticlesAsync(getSelectedItems()) {
            runLater { articleSummaryItemsView.clearSelectedItems() }
        }
    }

    private fun getSelectedItems(): Collection<ArticleSummaryItem> {
        return articleSummaryItemsView.checkedItems
    }


    private fun articleSummaryReceived(result: AsyncResult<out ArticleSummary>) {
        result.result?.let { articleSummaryReceived(it) }
        // TODO: show error
    }

    private fun articleSummaryReceived(articleSummary: ArticleSummary) {
        runLater {
            articleSummaryItemsView.showArticlesOnUiThread(articleSummary.articles, articleSummary.indexOfAddedItems)

            canLoadMoreItems.set(articleSummary.canLoadMoreItems)

            lastUpdateTime.set(LastUpdateTimeDateFormat.format(Date()))
        }
    }

}