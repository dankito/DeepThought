package net.dankito.deepthought.android.views

import android.view.View
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_item_fields.view.*


class EditItemActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, private val addTagsListener: () -> Unit, private val addSourceListener: () -> Unit,
                                               private val addTitleOrSummaryListener: () -> Unit, private val addFilesListener: () -> Unit) : FloatingActionMenuButton(floatingActionMenu) {


    init {
        setupUI()
    }

    private fun setupUI() {
        floatingActionMenu.fabEditItemTags.setOnClickListener { executeAndCloseMenu(addTagsListener) }
        floatingActionMenu.fabEditItemSource.setOnClickListener { executeAndCloseMenu(addSourceListener) }
        floatingActionMenu.fabEditItemSummary.setOnClickListener { executeAndCloseMenu(addTitleOrSummaryListener) }
        floatingActionMenu.fabEditItemFiles.setOnClickListener { executeAndCloseMenu(addFilesListener) }
    }


    fun setVisibilityOnUIThread(forceHideFloatingActionButton: Boolean = false, hasUserEverEnteredSomeContent: Boolean) {
        if(forceHideFloatingActionButton) {
            floatingActionMenu.floatingActionMenu.visibility = View.GONE
        }
        else if(hasUserEverEnteredSomeContent && isAnyFloatingActionMenuButtonVisible()) {
            floatingActionMenu.floatingActionMenu.visibility = View.VISIBLE
        }
        else {
            floatingActionMenu.floatingActionMenu.visibility = View.GONE
        }
    }

    private fun isAnyFloatingActionMenuButtonVisible(): Boolean {
        return  floatingActionMenu.fabEditItemTags.visibility != View.GONE ||
                floatingActionMenu.fabEditItemSource.visibility != View.GONE ||
                floatingActionMenu.fabEditItemSummary.visibility != View.GONE ||
                floatingActionMenu.fabEditItemFiles.visibility != View.GONE
    }

}