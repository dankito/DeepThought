package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_entries_list.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntityEntriesListPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


abstract class EntriesListDialogBase : FullscreenDialogFragment() {

    protected val presenter: EntityEntriesListPresenter

    protected val adapter: EntryAdapter


    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        presenter = EntityEntriesListPresenter(deleteEntityService, clipboardService, router)

        adapter = EntryAdapter(presenter)
    }


    override fun getLayoutId() = R.layout.dialog_entries_list

    override fun setupUI(rootView: View) {
        rootView.lstEntries.adapter = adapter

        rootView.lstEntries.setOnItemClickListener { _, _, position, _ -> router.showViewEntryView(adapter.getItem(position)) }
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        retrieveAndShowEntries()
    }


    protected fun retrieveAndShowEntries() {
        retrieveEntries { showEntries(it) }
    }

    private fun showEntries(entries: List<Entry>) {
        activity?.runOnUiThread { adapter.setItems(entries) }
    }

    protected abstract fun retrieveEntries(callback: (List<Entry>) -> Unit)


    fun showDialog(fragmentManager: FragmentManager) {
        showInFullscreen(fragmentManager, false)
    }

}