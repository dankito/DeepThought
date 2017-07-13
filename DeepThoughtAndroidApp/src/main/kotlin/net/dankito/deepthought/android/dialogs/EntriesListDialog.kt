package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_entries_list.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EntriesListDialog : FullscreenDialogFragment(), IEntriesListView {

    private val presenter: EntriesListPresenter

    private val adapter: EntryAdapter


    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)

        adapter = EntryAdapter(presenter)
    }


    override fun getLayoutId() = R.layout.dialog_entries_list

    override fun setupUI(rootView: View) {
        rootView.lstEntries.adapter = adapter

        rootView.lstEntries.setOnItemClickListener { _, _, position, _ -> router.showViewEntryView(adapter.getItem(position)) }
    }


    override fun showEntries(entries: List<Entry>) {
        // don't react to search results, we have a fixed set of entries to show
    }

    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        showEntries(entries)
    }


    fun showDialogForEntries(fragmentManager: FragmentManager, entries: List<Entry>) {
        adapter.setItems(entries)

        showInFullscreen(fragmentManager, false)
    }

}