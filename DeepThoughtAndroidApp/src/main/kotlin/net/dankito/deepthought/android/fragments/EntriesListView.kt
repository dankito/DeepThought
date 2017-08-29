package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.content.Context
import android.widget.BaseAdapter
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EntriesListView : MainActivityTabFragment(R.layout.fragment_tab_entries, R.id.lstEntries, R.menu.fragment_tab_entries_menu), IEntriesListView {

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    private val presenter: EntriesListPresenter

    private val entryAdapter: EntryAdapter

    private var entriesToShowOnAttach: List<Entry>? = null


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        entryAdapter = EntryAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return entryAdapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
        (selectedItem as? Entry)?.let { presenter.showEntry(it) }
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_entries)

    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        entriesToShowOnAttach?.let {
            showEntries(it)
            entriesToShowOnAttach = null
        }
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        val activity = this.activity

        if(activity != null) {
            activity.runOnUiThread {
                entryAdapter.setItems(entries)
            }
        }
        else {
            entriesToShowOnAttach = entries
        }
    }

    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showEntriesForTag(tag, tagsFilter)
    }

}