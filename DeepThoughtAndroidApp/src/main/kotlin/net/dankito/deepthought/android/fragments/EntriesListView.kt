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


class EntriesListView : MainActivityTabFragment<Entry>(R.menu.fragment_tab_entries_menu, R.menu.entry_contextual_action_menu, R.string.tab_entries_onboarding_text), IEntriesListView {

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    private val presenter: EntriesListPresenter

    private val entryAdapter: EntryRecyclerAdapter

    private var entriesToShowOnAttach: List<Entry>? = null

    var mainNavigationView: View? = null


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        entryAdapter = EntryRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<Entry, out RecyclerView.ViewHolder> {
        return entryAdapter
    }

    override fun listItemClicked(selectedItem: Entry) {
        presenter.showEntry(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<Entry>): Boolean {
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


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_entries)

    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        entriesToShowOnAttach?.let {
            showEntities(it)
            entriesToShowOnAttach = null
        }
    }


    override fun showOnboardingView() {
        super.showOnboardingView()

        mainNavigationView?.visibility = View.GONE
    }

    override fun hideOnboardingView() {
        super.hideOnboardingView()

        mainNavigationView?.visibility = View.VISIBLE
    }


    /*          IEntriesListView implementation            */

    override fun showEntities(entities: List<Entry>) {
        val activity = this.activity

        if(activity != null) {
            activity.runOnUiThread {
                entryAdapter.items = entities

                retrievedEntitiesOnUiThread(entities)
            }
        }
        else {
            entriesToShowOnAttach = entities
        }
    }

    // TODO: this is only used for JavaFX, remove it
    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showEntriesForTag(tag, tagsFilter)
    }

}