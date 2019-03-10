package net.dankito.deepthought.javafx.dialogs.articlesummary.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.SetChangeListener
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import net.dankito.deepthought.javafx.res.icons.Icons
import net.dankito.utils.javafx.ui.controls.searchtextfield
import net.dankito.utils.javafx.util.FXUtils
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.AsyncResult
import tornadofx.*
import java.text.DateFormat
import java.util.*


class ArticleSummaryControlBarView(private val presenter: ArticleSummaryPresenter, private val articleSummaryItemsView: ArticleSummaryItemsView,
                                   private val articleSummaryExtractorConfig: ArticleSummaryExtractorConfig) : View() {

    companion object {
        private const val ButtonsHeight = 40.0

        private const val TextButtonsWidth = 120.0

        private const val IconButtonsWidth = ButtonsHeight

        private const val ButtonsTopAndBottomMargin = 4.0

        private const val ButtonsLeftMargin = 12.0

        private const val BarHeight = ButtonsHeight + 2 * ButtonsTopAndBottomMargin

        private const val SearchBarWidth = 200.0

        private const val SearchFieldLeftMargin = IconButtonsWidth + 6.0

        private const val SearchFieldWidth = SearchBarWidth - SearchFieldLeftMargin

        private const val SearchFieldHeight = 36

        private const val SearchFieldTopAndBottomMargin = (BarHeight - SearchFieldHeight) / 2.0

        private val LastUpdateTimeDateFormat = DateFormat.getDateTimeInstance()
    }


    val canLoadMoreItems = SimpleBooleanProperty(false)

    val lastUpdateTime = SimpleStringProperty("")


    private val countItemsSelectedLabelText = SimpleStringProperty(String.format(messages["count.items.selected"], 0))

    private val areSelectedItemsActionButtonsDisabled = SimpleBooleanProperty(true)

    private val isSearchFieldVisible = SimpleBooleanProperty(false)


    init {
        articleSummaryItemsView.checkedItems.addListener(SetChangeListener<ArticleSummaryItem> {
            checkedItemsChanged(it)
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

            anchorpane {
                minWidth = SearchBarWidth
                maxWidth = SearchBarWidth

                label(lastUpdateTime) {
                    visibleProperty().bind(isSearchFieldVisible.not())

                    anchorpaneConstraints {
                        topAnchor = 0.0
                        rightAnchor = SearchFieldLeftMargin
                        bottomAnchor = 0.0
                    }
                }

                searchtextfield {
                    minWidth = SearchFieldWidth
                    maxWidth = SearchFieldWidth

                    visibleProperty().bind(isSearchFieldVisible)

                    textProperty().addListener { _, _, newValue ->
                        searchArticles(newValue)
                    }

                    setOnKeyReleased { event ->
                        if (event.code == KeyCode.ESCAPE && this.text.isEmpty()) { // when pressing escape in empty search field, hide search field
                            isSearchFieldVisible.set(false)
                        }
                        else if (checkShouldToggleSearchFieldVisibility(event) == false) {
                            this.handleKeyReleased(event)
                        }
                    }

                    visibleProperty().addListener { _, _, newValue ->
                        if (newValue) {
                            requestFocus()
                        }
                        else {
                            searchArticles("")
                        }
                    }

                    anchorpaneConstraints {
                        topAnchor = SearchFieldTopAndBottomMargin
                        rightAnchor = SearchFieldLeftMargin
                        bottomAnchor = SearchFieldTopAndBottomMargin
                    }
                }

                togglebutton {
                    minHeight = ButtonsHeight
                    maxHeight = ButtonsHeight
                    minWidth = IconButtonsWidth
                    maxWidth = IconButtonsWidth

                    graphic = ImageView(Icons.SearchIconPath)
                    contentDisplay = ContentDisplay.GRAPHIC_ONLY

                    selectedProperty().bindBidirectional(isSearchFieldVisible)

                    anchorpaneConstraints {
                        topAnchor = ButtonsTopAndBottomMargin
                        rightAnchor = 0.0
                        bottomAnchor = ButtonsTopAndBottomMargin
                    }
                }

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
                graphic = ImageView(Icons.UpdateIconPath)

                action { presenter.extractArticlesSummary(articleSummaryExtractorConfig) { articleSummaryReceived(it) } }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
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
                graphic = ImageView(Icons.LoadNextItemsIconPath)

                action { presenter.loadMoreItems(articleSummaryExtractorConfig) { articleSummaryReceived(it) } }

                hboxConstraints {
                    marginLeft = ButtonsLeftMargin
                    marginTopBottom(ButtonsTopAndBottomMargin)
                }
            }
        }

        articleSummaryItemsView.root.setOnKeyReleased { event ->
            checkShouldToggleSearchFieldVisibility(event)
        }
    }


    private fun checkShouldToggleSearchFieldVisibility(event: KeyEvent): Boolean {
        if (event.isControlDown && event.code == KeyCode.F) {
            isSearchFieldVisible.set(!!! isSearchFieldVisible.get())

            return true
        }

        return false
    }

    private fun searchArticles(query: String) {
        articleSummaryItemsView.showArticlesOnUiThread(presenter.searchArticleSummaryItems(query), 0)
    }


    private fun viewSelectedItems() {
        presenter.getAndShowArticlesAsync(getSelectedItems()) {

        }
    }

    private fun saveSelectedItemsForLaterReading() {
        presenter.getAndSaveArticlesForLaterReadingAsync(getSelectedItems()) {

        }
    }

    private fun saveSelectedItems() {
        presenter.getAndSaveArticlesAsync(getSelectedItems()) {

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


    private fun checkedItemsChanged(change: SetChangeListener.Change<out ArticleSummaryItem>) {
        runLater { // don't know how this ever could happen, but got called on non UI thread
            areSelectedItemsActionButtonsDisabled.set(change.set.size == 0)

            countItemsSelectedLabelText.set(String.format(messages["count.items.selected"], change.set.size))
        }
    }

}