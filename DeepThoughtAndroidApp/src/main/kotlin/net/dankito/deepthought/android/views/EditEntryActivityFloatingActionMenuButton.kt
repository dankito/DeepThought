package net.dankito.deepthought.android.views

import android.view.View
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_entry_fields.view.*


class EditEntryActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, private val addTagsListener: () -> Unit, private val addReferenceListener: () -> Unit,
                                                private val addTitleOrAbstractListener: () -> Unit) : FloatingActionMenuButton(floatingActionMenu) {


    init {
        setupUI()
    }

    private fun setupUI() {
        floatingActionMenu.fabEditEntryTags.setOnClickListener { executeAndCloseMenu { addTagsListener() } }
        floatingActionMenu.fabEditEntryReference.setOnClickListener { executeAndCloseMenu { addReferenceListener() } }
        floatingActionMenu.fabEditEntryAbstract.setOnClickListener { executeAndCloseMenu { addTitleOrAbstractListener() } }
    }


    fun setVisibilityOnUIThread(isInFullscreenMode: Boolean = false) {
        if(isInFullscreenMode) {
            floatingActionMenu.floatingActionMenu.visibility = View.GONE
        }
        else if(floatingActionMenu.fabEditEntryTags.visibility != View.GONE || floatingActionMenu.fabEditEntryReference.visibility != View.GONE ||
                floatingActionMenu.fabEditEntryAbstract.visibility != View.GONE) {
            floatingActionMenu.floatingActionMenu.visibility = View.VISIBLE
        }
        else {
            floatingActionMenu.floatingActionMenu.visibility = View.GONE
        }
    }

}