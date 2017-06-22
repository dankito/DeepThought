package net.dankito.deepthought.android.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.MainActivityTabFragment
import net.dankito.deepthought.android.fragments.TagsListView


class MainActivitySectionsPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


    private val entriesListView = EntriesListView()

    private val tagsListView = TagsListView()


    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): MainActivityTabFragment {
        when(position) {
            0 -> return entriesListView
            1 -> return tagsListView
            else -> return entriesListView // fallback, but should never happen
        }
    }

}