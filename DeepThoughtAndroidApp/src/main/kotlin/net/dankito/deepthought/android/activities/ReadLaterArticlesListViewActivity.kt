package net.dankito.deepthought.android.activities

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.ReferencesListView

class ReadLaterArticlesListViewActivity : EntitiesListViewActivityBase<ReferencesListView>(R.string.nav_menu_read_later_articles_title, false) {

    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return ReadLaterArticlesListView()
    }

}