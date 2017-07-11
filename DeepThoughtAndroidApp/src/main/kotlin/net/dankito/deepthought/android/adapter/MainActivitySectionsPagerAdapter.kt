package net.dankito.deepthought.android.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.dankito.deepthought.android.fragments.*


class MainActivitySectionsPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


    private val entriesListView = EntriesListView()

    private val tagsListView = TagsListView()

    private val referencesListView = ReferencesListView()

    private val readLaterArticlesListView = ReadLaterArticlesListView()


    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): MainActivityTabFragment {
        when(position) {
            0 -> return entriesListView
            1 -> return tagsListView
            2 -> return referencesListView
            3 -> return readLaterArticlesListView
            else -> return entriesListView // to make compiler happy
        }
    }

}