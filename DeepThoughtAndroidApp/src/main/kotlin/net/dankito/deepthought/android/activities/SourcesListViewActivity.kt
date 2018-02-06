package net.dankito.deepthought.android.activities

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.SourcesListView


class SourcesListViewActivity : EntitiesListViewActivityBase<SourcesListView>(R.string.nav_menu_sources_title) {

    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return SourcesListView()
    }

}