package net.dankito.deepthought.android.fragments

import android.widget.BaseAdapter
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ReadLaterArticlesAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ReadLaterArticlePresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject


class ReadLaterArticlesListView : MainActivityTabFragment(R.layout.fragment_tab_read_later_articles, R.id.lstReadLaterArticles, R.menu.fragment_tab_read_later_articles_menu), IReadLaterArticleView {


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticlePresenter

    private val adapter = ReadLaterArticlesAdapter()


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticlePresenter(this, searchEngine, readLaterArticleService, entryPersister, router)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return adapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
        (selectedItem as? ReadLaterArticle)?.let { presenter.showArticle(it) }
    }

    override fun getQueryHint() = activity.getString(R.string.search_hint_read_later_articles)

    override fun searchEntities(query: String) {
        presenter.getReadLaterArticles(query)
    }


    /*      IReadLaterArticleView implementation        */

    override fun showArticles(readLaterArticles: List<ReadLaterArticle>) {
        activity.runOnUiThread {
            adapter.setItems(readLaterArticles)
        }
    }

}