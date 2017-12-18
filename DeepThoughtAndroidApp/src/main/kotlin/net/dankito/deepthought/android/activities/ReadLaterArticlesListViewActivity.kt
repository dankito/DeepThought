package net.dankito.deepthought.android.activities

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView


class ReadLaterArticlesListViewActivity : EntitiesListViewActivityBase<ReadLaterArticlesListView>(R.string.nav_menu_read_later_articles_title) {

    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return ReadLaterArticlesListView()
    }

}