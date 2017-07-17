package net.dankito.deepthought.android.fragments

import android.widget.BaseAdapter
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.TagAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject


class TagsListView : MainActivityTabFragment(R.layout.fragment_tab_tags, R.id.lstTags, R.menu.fragment_tab_tags_menu), ITagsListView {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter


    private lateinit var presenter: TagsListPresenter

    private lateinit var adapter: TagAdapter


    init {
        AppComponent.component.inject(this)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = TagsListPresenter(this, dataManager, searchEngine, router)

        adapter = TagAdapter(presenter)

        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return adapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
        tagSelected(selectedItem as? Tag)
    }


    override fun getQueryHint() = activity.getString(R.string.search_hint_tags)

    override fun searchEntities(query: String) {
        presenter.searchTags(query)
    }


    private fun tagSelected(selectedTag: Tag?) {
        if(selectedTag != null) {
            presenter.showEntriesForTag(selectedTag)
        }
        else {
//            presenter.clearSelectedTag() // TODO
        }
    }


    /*          ITagsListView implementation            */

    override fun showTags(tags: List<Tag>) {
        activity?.runOnUiThread {
            adapter.setItems(tags)
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread  { adapter.notifyDataSetChanged() }
    }

}