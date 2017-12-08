package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EntriesListView : EntitiesListViewFragment<Item>(R.menu.entry_contextual_action_menu, R.string.tab_items_onboarding_text), IEntriesListView {

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    private val presenter: EntriesListPresenter

    private val entryAdapter: EntryRecyclerAdapter

    private var entriesToShowOnAttaches: List<Item>? = null


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        entryAdapter = EntryRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<Item, out RecyclerView.ViewHolder> {
        return entryAdapter
    }

    override fun listItemClicked(selectedItem: Item) {
        presenter.showEntry(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<Item>): Boolean {
        when(actionItem.itemId) {
            R.id.mnEditEntry -> {
                selectedItems.forEach { presenter.showEntry(it) }
                mode.finish()
                return true
            }
            R.id.mnDeleteEntry -> {
                selectedItems.forEach { presenter.deleteEntry(it) }
                mode.finish()
                return true
            }
            else -> return false
        }
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_items)

    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        entriesToShowOnAttaches?.let {
            showEntities(it)
            entriesToShowOnAttaches = null
        }
    }


    override fun showOnboardingView() {
        super.showOnboardingView()

        arrowToFloatingActionButton.visibility = View.VISIBLE

        // show txtOnboardingText a little bit above the center as it looks more natural and leaves more room for arrowToFloatingActionButton
        val translationY = 50 * context.resources.displayMetrics.density
        txtOnboardingText?.translationY = -1 * translationY
        vwStartingWhereTranslatedTextViewOnboardingTextEnds.layoutParams.height = translationY.toInt()
    }

    override fun hideOnboardingView() {
        super.hideOnboardingView()

        if(entryAdapter.isInMultiSelectMode() == false) {
            arrowToFloatingActionButton.visibility = View.GONE
        }
    }


    /*          IEntriesListView implementation            */

    override fun showEntities(entities: List<Item>) {
        val activity = this.activity

        if(activity != null) {
            activity.runOnUiThread {
                entryAdapter.items = entities

                retrievedEntitiesOnUiThread(entities)
            }
        }
        else {
            entriesToShowOnAttaches = entities
        }
    }

    // TODO: this is only used for JavaFX, remove it
    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showEntriesForTag(tag, tagsFilter)
    }

}