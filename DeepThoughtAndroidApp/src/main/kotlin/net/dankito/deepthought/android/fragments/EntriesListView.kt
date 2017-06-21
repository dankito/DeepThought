package net.dankito.deepthought.android.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_tab_entries.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.search.ISearchEngine


class EntriesListView(private val searchEngine: ISearchEngine, private val router: IRouter) : Fragment(), IEntriesListView {

    private lateinit var presenter: EntriesListPresenter

    private val entryAdapter = EntryAdapter()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_tab_entries, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun setupUI(rootView: View?) {
        rootView?.lstEntries?.adapter = entryAdapter
        rootView?.lstEntries?.setOnItemClickListener { _, _, position, _ -> presenter.showEntry(entryAdapter.getItem(position)) }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        presenter = EntriesListPresenter(this, router, searchEngine)
    }

    override fun onDestroy() {
        presenter.cleanUp()

        super.onDestroy()
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        activity.runOnUiThread {
            entryAdapter.setItems(entries)
        }
    }

}