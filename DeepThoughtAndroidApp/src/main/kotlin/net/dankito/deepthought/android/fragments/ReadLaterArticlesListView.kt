package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.MenuItem
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.ReadLaterArticleRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter
import net.dankito.deepthought.ui.view.IReadLaterArticleView
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ReadLaterArticlesListView : EntitiesListViewFragment<ReadLaterArticle>(R.menu.read_later_article_contextual_action_menu,
        R.string.tab_read_later_articles_onboarding_text), IReadLaterArticleView {


    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var itemPersister: ItemPersister

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    private val presenter: ReadLaterArticleListPresenter

    private val adapter: ReadLaterArticleRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        presenter = ReadLaterArticleListPresenter(this, searchEngine, readLaterArticleService, itemPersister, clipboardService, router)

        adapter = ReadLaterArticleRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<ReadLaterArticle, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun listItemClicked(selectedItem: ReadLaterArticle) {
        presenter.showArticle(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<ReadLaterArticle>): Boolean {
        when(actionItem.itemId) {
            R.id.mnSaveReadLaterArticle -> {
                selectedItems.forEach { presenter.saveAndDeleteReadLaterArticle(it) }
                mode.finish()
                return true
            }
            R.id.mnDeleteReadLaterArticle -> {
                selectedItems.forEach { presenter.deleteReadLaterArticle(it) }
                mode.finish()
                return true
            }
            else -> return false
        }
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