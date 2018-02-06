package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_items_list.*
import kotlinx.android.synthetic.main.dialog_items_list.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntityItemsListPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


abstract class ItemsListDialogBase : FullscreenDialogFragment() {

    protected val presenter: EntityItemsListPresenter

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

        presenter = EntityItemsListPresenter(deleteEntityService, clipboardService, router)

        adapter = EntryRecyclerAdapter(presenter)
    }


    override fun getLayoutId() = R.layout.dialog_items_list

    override fun setupUI(rootView: View) {
        val context = rootView.context
        rootView.rcyItems.addItemDecoration(HorizontalDividerItemDecoration(context))
        rootView.rcyItems.adapter = adapter

        adapter.itemClickListener = { item -> router.showEditItemView(item) }
    }


    override fun onResume() {
        super.onResume()

        retrieveAndShowItems()
    }

    protected fun retrieveAndShowItems() {
        retrieveItems { items ->
            activity?.runOnUiThread {
                showItemsOnUiThread(items)
                showDialogTitleOnUiThread(items)
            }
        }
    }

    private fun showItemsOnUiThread(items: List<Item>) {
        adapter.items = items
    }

    private fun showDialogTitleOnUiThread(items: List<Item>) {
        toolbar?.let { toolbar -> // sometimes toolbar is null (why?)
            toolbar.title = getDialogTitle(items)
        }
    }

    protected open fun getDialogTitle(items: List<Item>): String {
        return ""
    }

    protected abstract fun retrieveItems(callback: (List<Item>) -> Unit)


    fun showDialog(fragmentManager: FragmentManager) {
        showInFullscreen(fragmentManager, false)
    }

}