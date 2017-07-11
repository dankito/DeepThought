package net.dankito.deepthought.android.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.MainActivityTabFragment
import net.dankito.deepthought.android.fragments.ReferencesListView
import net.dankito.deepthought.android.fragments.TagsListView


class MainActivitySectionsPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


    private val entriesListView = EntriesListView()

    private val tagsListView = TagsListView()

    private val referencesListView = ReferencesListView()


    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): MainActivityTabFragment {
        when(position) {
            0 -> return entriesListView
            1 -> return tagsListView
            2 -> return referencesListView
            else -> return entriesListView // to make compiler happy
        }
    }

}