package net.dankito.deepthought.android.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import kotlinx.android.synthetic.main.fragment_tab_tags.view.*
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


class TagsListView : MainActivityTabFragment(), ITagsListView {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter


    private lateinit var presenter: TagsListPresenter

    private val adapter = TagAdapter()


    init {
        AppComponent.component.inject(this)
    }



    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_tab_tags
    }

    override fun setupUI(rootView: View?) {
        rootView?.lstTags?.adapter = adapter
        rootView?.lstTags?.setOnItemClickListener { _, _, position, _ -> tagSelected(adapter.getItem(position)) }
    }

    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = TagsListPresenter(this, dataManager, searchEngine, router)

        return presenter
    }


    override fun getHasOptionsMenu(): Boolean {
        return true
    }

    override fun initOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_tab_tags_menu, menu)
    }

    override fun getQueryHint() = activity.getString(R.string.search_hint_tags)

    override fun searchEntities(query: String) {
        presenter.searchTags(query)
    }


    private fun tagSelected(selectedTag: Tag?) {
        if(selectedTag != null) {
            // TODO: when tag filter is applied only pass filtered entries to showEntriesForTag()
            presenter.showEntriesForTag(selectedTag, selectedTag.entries)
        }
        else {
//            presenter.clearSelectedTag() // TODO
        }
    }


    /*          ITagsListView implementation            */

    override fun showTags(tags: List<Tag>) {
        activity.runOnUiThread {
            adapter.setItems(tags)
        }
    }

    override fun updateDisplayedTags() {
        activity.runOnUiThread  { adapter.notifyDataSetChanged() }
    }

}