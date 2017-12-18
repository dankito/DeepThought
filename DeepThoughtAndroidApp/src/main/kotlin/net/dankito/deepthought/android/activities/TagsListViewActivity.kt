package net.dankito.deepthought.android.activities

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.TagsListView


class TagsListViewActivity : EntitiesListViewActivityBase<TagsListView>(R.string.nav_menu_tags_title) {

    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return TagsListView()
    }

}