package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ItemRecyclerAdapter
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ItemsListPresenter
import net.dankito.deepthought.ui.view.IItemsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.FieldName
import net.dankito.service.search.util.SortOption
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ItemsListView : EntitiesListViewFragment<Item>(R.menu.item_contextual_action_menu, R.string.tab_items_onboarding_text), IItemsListView {

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var threadPool: IThreadPool


    private val presenter: ItemsListPresenter

    private val itemAdapter: ItemRecyclerAdapter

    private var itemsToShowOnAttach: List<Item>? = null


    init {
        AppComponent.component.inject(this)

        presenter = ItemsListPresenter(this, router, searchEngine, deleteEntityService, clipboardService, threadPool)
        itemAdapter = ItemRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<Item, out RecyclerView.ViewHolder> {
        return itemAdapter
    }

    override fun listItemClicked(selectedItem: Item) {
        presenter.showItem(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<Item>): Boolean {
        when(actionItem.itemId) {
            R.id.mnEditItem -> {
                selectedItems.forEach { presenter.showItem(it) }
                mode.finish()
                return true
            }
            R.id.mnDeleteItem -> {
                presenter.deleteItemsAsync(selectedItems)
                mode.finish()
                return true
            }
            else -> return false
        }
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_items)

    override fun searchEntities(query: String) {
        presenter.searchItems(query)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        itemsToShowOnAttach?.let {
            showEntities(it)
            itemsToShowOnAttach = null
        }
    }


    override fun showOnboardingView() {
        super.showOnboardingView()

        arrowToFloatingActionButton.visibility = View.VISIBLE

        // show txtOnboardingText a little bit above the center as it looks more natural and leaves more room for arrowToFloatingActionButton
        context?.let { context ->
            val translationY = 50 * context.resources.displayMetrics.density
            txtOnboardingText?.translationY = -1 * translationY
            vwStartingWhereTranslatedTextViewOnboardingTextEnds.layoutParams.height = translationY.toInt()
        }
    }

    override fun hideOnboardingView() {
        super.hideOnboardingView()

        if(itemAdapter.isInMultiSelectMode() == false) {
            arrowToFloatingActionButton.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_items_list_view_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setSortMenuItemCheckState(menu, R.id.mnSortItemsByPreviewAscending, FieldName.ItemPreviewForSorting, true)
        setSortMenuItemCheckState(menu, R.id.mnSortItemsByPreviewDescending, FieldName.ItemPreviewForSorting, false)

        setSortMenuItemCheckState(menu, R.id.mnSortItemsBySourcePreviewAscending, FieldName.ItemSourcePreviewForSorting, true)
        setSortMenuItemCheckState(menu, R.id.mnSortItemsBySourcePreviewDescending, FieldName.ItemSourcePreviewForSorting, false)
    }

    private fun setSortMenuItemCheckState(menu: Menu, menuItemId: Int, propertyName: String, ascending: Boolean) {
        menu.findItem(menuItemId)?.let {
            it.isChecked = presenter.isSortOptionSet(propertyName, ascending)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnSortItemsByPreviewAscending -> setSortOptions(FieldName.ItemPreviewForSorting, true)
            R.id.mnSortItemsByPreviewDescending -> setSortOptions(FieldName.ItemPreviewForSorting, false)
            R.id.mnSortItemsBySourcePreviewAscending -> setSortOptions(FieldName.ItemSourcePreviewForSorting, true)
            R.id.mnSortItemsBySourcePreviewDescending -> setSortOptions(FieldName.ItemSourcePreviewForSorting, false)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setSortOptions(property: String, ascending: Boolean): Boolean {
        presenter.setSortOptionsAsync(listOf(SortOption(property, ascending)))

        return true
    }


    /*          IItemsListView implementation            */

    override fun showEntities(entities: List<Item>) {
        val activity = this.activity

        if(activity != null) {
            activity.runOnUiThread {
                itemAdapter.items = entities

                retrievedEntitiesOnUiThread(entities)
            }
        }
        else {
            itemsToShowOnAttach = entities
        }
    }

}