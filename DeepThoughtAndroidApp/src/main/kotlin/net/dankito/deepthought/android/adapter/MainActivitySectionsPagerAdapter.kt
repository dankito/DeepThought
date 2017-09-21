package net.dankito.deepthought.android.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import net.dankito.deepthought.android.fragments.*
import net.dankito.deepthought.model.BaseEntity


class MainActivitySectionsPagerAdapter(private val fragmentManager: FragmentManager, private val mainNavigationView: View) : FragmentPagerAdapter(fragmentManager) {


    private var tagsListView: TagsListView? = null

    private var referencesListView: ReferencesListView? = null

    private var readLaterArticlesListView: ReadLaterArticlesListView? = null


    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): MainActivityTabFragment<out BaseEntity> {
        when(position) {
            0 -> return getEntriesListView() // EntriesListView always seems to work
            1 -> return getTagsListView()
            2 -> return getReferencesListView()
            3 -> return getReadLaterArticlesListView()
            else -> return getEntriesListView() // to make compiler happy
        }
    }

    /**
     * I couldn't figure out the issue yet: When you navigate away from MainActivity, e.g. to ViewEntryActivity, and MainActivity gets destroyed in the meantime,
     * some fragments get created by system - completely independent from MainActivitySectionsPagerAdapter -, then MainActivity gets created and therefor in
     * MainActivitySectionsPagerAdapter creates another instance of these fragments. But the issue with the ladder ones is, that their activity property doesn't get set
     * -> no entities can be displayed.
     * Therefor i now check via FragmentManager first if fragments of this instance already exist. If so i use these, if not then i create a new one.
     * Makes the code much more complex but works.
     */

    private fun getEntriesListView(): EntriesListView {
        // couldn't figure out that Android bug: after destroying MainActivity, on create two EntriesListViews are created: One by Android system and one here
        // On the one created here activity and context never are set, so it can't display its data. Therefore never return this one, always use fragmentManager.
        // This is only true for EntriesListView (really curious; maybe due to it's the first fragment in adapter)

        fragmentManager.fragments.forEach { fragment ->
            if(fragment is EntriesListView) {
                fragment.mainNavigationView = mainNavigationView
                return fragment
            }
        }

        val entriesListView = EntriesListView()
        entriesListView.mainNavigationView = mainNavigationView

        return entriesListView
    }

    private fun getTagsListView(): TagsListView {
        tagsListView?.let {
            return it
        }

        fragmentManager.fragments.forEach { fragment ->
            if(fragment is TagsListView) {
                tagsListView = fragment
            }
        }

        if(tagsListView == null) {
            tagsListView = TagsListView()
        }

        return tagsListView!!
    }

    private fun getReferencesListView(): ReferencesListView {
        referencesListView?.let {
            return it
        }

        fragmentManager.fragments.forEach { fragment ->
            if(fragment is ReferencesListView) {
                referencesListView = fragment
            }
        }

        if(referencesListView == null) {
            referencesListView = ReferencesListView()
        }

        return referencesListView!!
    }

    private fun getReadLaterArticlesListView(): ReadLaterArticlesListView {
        readLaterArticlesListView?.let {
            return it
        }

        fragmentManager.fragments.forEach { fragment ->
            if(fragment is ReadLaterArticlesListView) {
                readLaterArticlesListView = fragment
            }
        }

        if(readLaterArticlesListView == null) {
            readLaterArticlesListView = ReadLaterArticlesListView()
        }

        return readLaterArticlesListView!!
    }

}