package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryControlBarView
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryWindowData
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
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

    private val extractorConfig: ArticleSummaryExtractorConfig



    override val root = borderpane {

    }


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryPresenter(itemPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)

        this.extractorConfig = (windowData as ArticleSummaryWindowData).articleSummaryExtractorConfig

        (windowData as ArticleSummaryWindowData).articleSummary?.let { articleSummary ->
            presenter.retrievedArticleSummary(articleSummary, extractorConfig)
        }

        initUi()
    }

    private fun initUi() {
        val articleSummaryItemsView = ArticleSummaryItemsView(presenter)
        root.center = articleSummaryItemsView.root

        val articleSummaryControlBarView = ArticleSummaryControlBarView(presenter, articleSummaryItemsView, extractorConfig)
        root.bottom = articleSummaryControlBarView.root

        title = extractorConfig.name
    }


    override val windowDataClass = ArticleSummaryWindowData::class.java

    override fun getCurrentWindowData(): Any? {
        (windowData as ArticleSummaryWindowData).articleSummary = presenter.lastLoadedSummary

        return windowData
    }

//    override fun getWindowData(): Any? {
//        extractorConfig?.let {
//            return ArticleSummaryWindowData(it, presenter.lastLoadedSummary)
//        }
//
//        return null
//    }

}