package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
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


    override fun setupUI(rootView: View?) {
        super.setupUI(rootView)

        rootView?.let {
            registerForContextMenu(rootView.rcyEntities)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_read_later_article_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position?.let { position ->
            if(position >= adapter.itemCount) {
                return super.onContextItemSelected(item)
            }

            val selectedReadLaterArticle = adapter.getItem(position)

            when(item.itemId) {
                R.id.mnSaveReadLaterArticle -> {
                    presenter.saveAndDeleteReadLaterArticle(selectedReadLaterArticle)
                    return true
                }
                R.id.mnShareReadLaterArticle -> { // TODO: actually there should also be the option to share article's text
                    presenter.copyReferenceUrlToClipboard(selectedReadLaterArticle)
                    return true
                }
                R.id.mnDeleteReadLaterArticle -> {
                    presenter.deleteReadLaterArticle(selectedReadLaterArticle)
                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }

        return super.onContextItemSelected(item)
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