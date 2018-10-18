package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryControlBarView
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.borderpane
import javax.inject.Inject


class ArticleSummaryView : DialogFragment() {

    @Inject
    protected lateinit var itemPersister: ItemPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var dialogService: IDialogService


    private val presenter: ArticleSummaryPresenter


    val articleSummaryExtractorConfig: ArticleSummaryExtractorConfig by param()

    private val articleSummaryItemsView: ArticleSummaryItemsView

    private val articleSummaryControlBarView: ArticleSummaryControlBarView



    override val root = borderpane {

    }


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryPresenter(itemPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)

        articleSummaryItemsView = ArticleSummaryItemsView(presenter)
        root.center = articleSummaryItemsView.root

        articleSummaryControlBarView = ArticleSummaryControlBarView(presenter, articleSummaryItemsView, articleSummaryExtractorConfig)
        root.bottom = articleSummaryControlBarView.root

        title = articleSummaryExtractorConfig.name
    }

}