package net.dankito.deepthought.android.views

import android.view.View
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_entry_fields.view.*


class EditEntryActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, private val addReferenceListener: () -> Unit,
                                                private val addTitleOrAbstractListener: () -> Unit) : FloatingActionMenuButton(floatingActionMenu) {


    init {
        setupUI()
    }

    private fun setupUI() {
        floatingActionMenu.fabEditEntryReference.setOnClickListener { executeAndCloseMenu { addReferenceListener() } }
        floatingActionMenu.fabEditEntryAbstract.setOnClickListener { executeAndCloseMenu { addTitleOrAbstractListener() } }
    }


    fun setVisibilityOnUIThread(isInFullscreenMode: Boolean = false) {
        if(isInFullscreenMode) {
            floatingActionMenu.fabEntryFieldsMenu.visibility = View.GONE
        }
        else if(floatingActionMenu.fabEditEntryReference.visibility != View.GONE || floatingActionMenu.fabEditEntryAbstract.visibility != View.GONE) {
            floatingActionMenu.fabEntryFieldsMenu.visibility = View.VISIBLE
        }
        else {
            floatingActionMenu.fabEntryFieldsMenu.visibility = View.GONE
        }
    }

}