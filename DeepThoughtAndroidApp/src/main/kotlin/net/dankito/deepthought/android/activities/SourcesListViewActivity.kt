package net.dankito.deepthought.android.activities

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.ReferencesListView


class SourcesListViewActivity : EntitiesListViewActivityBase<ReferencesListView>(R.string.nav_menu_sources_title) {

    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return ReferencesListView()
    }

}