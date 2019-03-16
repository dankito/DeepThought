package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_items_list.*
import kotlinx.android.synthetic.main.dialog_items_list.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ItemRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntityItemsListPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool
import net.dankito.utils.android.extensions.addHorizontalDividerItemDecoration
import net.dankito.utils.android.ui.dialogs.FullscreenDialogFragment
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import javax.inject.Inject


abstract class ItemsListDialogBase : FullscreenDialogFragment() {

    protected val presenter: EntityItemsListPresenter

    protected val adapter: ItemRecyclerAdapter


    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        AppComponent.component.inject(this)

        presenter = EntityItemsListPresenter(deleteEntityService, dialogService, clipboardService, router, threadPool)

        adapter = ItemRecyclerAdapter(presenter)
    }


    override fun getLayoutId() = R.layout.dialog_items_list

    override fun setupUI(rootView: View) {
        rootView.rcyItems.addHorizontalDividerItemDecoration()
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