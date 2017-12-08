package net.dankito.deepthought.android.dialogs

import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.deepthought.android.fragments.TagsListView

class TagsListViewDialog : EntitiesListViewDialogBase<TagsListView>() {

    companion object {
        val Tag = TagsListViewDialog::class.java.name
    }


    override fun instantiateListViewFragment(): EntitiesListViewFragment<*> {
        return TagsListView()
    }

    override fun getTitleResourceId() = R.string.nav_menu_tags_title

    override fun getDialogTag(): String {
        return Tag
    }

}