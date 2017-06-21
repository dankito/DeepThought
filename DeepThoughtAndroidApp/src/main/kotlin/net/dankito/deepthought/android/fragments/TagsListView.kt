package net.dankito.deepthought.android.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.TagAdapter
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.search.ISearchEngine
import kotlinx.android.synthetic.main.fragment_tab_tags.view.*


class TagsListView(private val searchEngine: ISearchEngine, private val router: IRouter) : Fragment(), ITagsListView {

    private lateinit var presenter: TagsListPresenter

    private val adapter = TagAdapter()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_tab_tags, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun setupUI(rootView: View?) {
        rootView?.lstTags?.adapter = adapter
        rootView?.lstTags?.setOnItemClickListener { _, _, position, _ -> presenter.showEntriesForTag(adapter.getItem(position)) }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        presenter = TagsListPresenter(this, router, searchEngine)
    }

    override fun onDestroy() {
        presenter.cleanUp()

        super.onDestroy()
    }


    /*          ITagsListView implementation            */

    override fun showTags(tags: List<Tag>) {
        activity.runOnUiThread {
            adapter.setItems(tags)
        }
    }

}