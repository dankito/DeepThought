package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_entries_list.*
import kotlinx.android.synthetic.main.dialog_entries_list.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
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

    protected val adapter: EntryRecyclerAdapter


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

        adapter = EntryRecyclerAdapter(presenter)
    }


    override fun getLayoutId() = R.layout.dialog_entries_list

    override fun setupUI(rootView: View) {
        val context = rootView.context
        rootView.rcyEntries.addItemDecoration(HorizontalDividerItemDecoration(context))
        rootView.rcyEntries.adapter = adapter

        adapter.itemClickListener = { entry -> router.showEditEntryView(entry) }
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        retrieveAndShowEntries()
    }


    protected fun retrieveAndShowEntries() {
        retrieveEntries {
            activity?.runOnUiThread {
                showEntriesOnUiThread(it)
                showDialogTitleOnUiThread(it)
            }
        }
    }

    private fun showEntriesOnUiThread(entries: List<Entry>) {
        adapter.items = entries
    }

    private fun showDialogTitleOnUiThread(entries: List<Entry>) {
        toolbar?.let { toolbar -> // sometimes toolbar is null (why?)
            toolbar.title = getDialogTitle(entries)
        }
    }

    protected open fun getDialogTitle(entries: List<Entry>): String {
        return ""
    }

    protected abstract fun retrieveEntries(callback: (List<Entry>) -> Unit)


    fun showDialog(fragmentManager: FragmentManager) {
        showInFullscreen(fragmentManager, false)
    }

}