package net.dankito.deepthought.android.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R


class TagsListView : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_tab_tags, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun setupUI(rootView: View?) {
//        lstTags.adapter = entryAdapter
//        lstTags.setOnItemClickListener { _, _, position, _ -> presenter.showEntry(entryAdapter.getItem(position)) }
    }

}