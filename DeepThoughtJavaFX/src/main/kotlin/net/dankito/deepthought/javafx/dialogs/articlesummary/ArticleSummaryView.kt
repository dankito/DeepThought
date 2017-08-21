package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryControlBarView
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.JavaFXArticleSummaryPresenter
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import javax.inject.Inject


class ArticleSummaryView : DialogFragment() {

    @Inject
    protected lateinit var entryPerister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var dialogService: IDialogService


    val articleSummaryExtractor: ArticleSummaryExtractorConfig by param()

    private var presenter: JavaFXArticleSummaryPresenter

    private val articleSummaryItemsView: ArticleSummaryItemsView

    private val articleSummaryControlBarView: ArticleSummaryControlBarView



    override val root = borderpane {

    }


    init {
        AppComponent.component.inject(this)

        presenter = JavaFXArticleSummaryPresenter(articleSummaryExtractor, entryPerister, readLaterArticleService, articleExtractorManager, router, dialogService)

        articleSummaryItemsView = ArticleSummaryItemsView(presenter)
        root.center = articleSummaryItemsView.root

        articleSummaryControlBarView = ArticleSummaryControlBarView(presenter, articleSummaryItemsView)
        root.bottom = articleSummaryControlBarView.root

        title = articleSummaryExtractor.name
    }

}