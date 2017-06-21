package net.dankito.deepthought.android.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine


class MainActivitySectionsPagerAdapter(fragmentManager: FragmentManager, searchEngine: ISearchEngine, router: IRouter)
    : FragmentPagerAdapter(fragmentManager) {


    private val entriesListView = EntriesListView(searchEngine, router)

    private val tagsListView = TagsListView(searchEngine, router)


    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> return entriesListView
            1 -> return tagsListView
            else -> return entriesListView // fallback, but should never happen
        }
    }

}