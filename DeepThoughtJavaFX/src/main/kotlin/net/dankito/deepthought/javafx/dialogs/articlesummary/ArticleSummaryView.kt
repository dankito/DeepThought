package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryControlBarView
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import tornadofx.*
import javax.inject.Inject


class ArticleSummaryView : DialogFragment() {

    @Inject
    protected lateinit var presenter: ArticleSummaryPresenter


    val articleSummaryExtractorConfig: ArticleSummaryExtractorConfig by param()

    private val articleSummaryItemsView: ArticleSummaryItemsView

    private val articleSummaryControlBarView: ArticleSummaryControlBarView



    override val root = borderpane {

    }


    init {
        AppComponent.component.inject(this)

        articleSummaryItemsView = ArticleSummaryItemsView(presenter)
        root.center = articleSummaryItemsView.root

        articleSummaryControlBarView = ArticleSummaryControlBarView(presenter, articleSummaryItemsView, articleSummaryExtractorConfig)
        root.bottom = articleSummaryControlBarView.root

        title = articleSummaryExtractorConfig.name
    }

}