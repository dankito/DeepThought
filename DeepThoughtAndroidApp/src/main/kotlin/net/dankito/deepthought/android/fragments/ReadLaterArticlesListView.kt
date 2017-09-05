package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.ReadLaterArticleRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ReadLaterArticlesListView : MainActivityTabFragment(R.menu.fragment_tab_read_later_articles_menu, R.string.tab_read_later_articles_onboarding_text), IReadLaterArticleView {


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticleListPresenter

    private val adapter: ReadLaterArticleRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticleListPresenter(this, searchEngine, readLaterArticleService, entryPersister, clipboardService, router)

        adapter = ReadLaterArticleRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): ListRecyclerSwipeAdapter<out BaseEntity, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun listItemClicked(selectedItem: BaseEntity) {
        (selectedItem as? ReadLaterArticle)?.let { presenter.showArticle(it) }
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_read_later_articles)

    override fun searchEntities(query: String) {
        presenter.getReadLaterArticles(query)
    }


    /*      IReadLaterArticleView implementation        */

    override fun showEntities(entities: List<ReadLaterArticle>) {
        activity?.runOnUiThread {
            adapter.items = entities

            retrievedEntitiesOnUiThread(entities)
        }
    }

}