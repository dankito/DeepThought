package net.dankito.deepthought.android.views

import android.view.MotionEvent
import com.github.clans.fab.FloatingActionMenu


open class FloatingActionMenuButton(protected val floatingActionMenu: FloatingActionMenu) {


    init {
        setup()
    }


    private fun setup() {
        floatingActionMenu.setClosedOnTouchOutside(true)
    }


    protected fun executeAndCloseMenu(action: () -> Unit) {
        action()
        closeMenu()
    }

    private fun closeMenu() {
        floatingActionMenu.close(true)
    }


    fun handlesBackButtonPress(): Boolean {
        if(floatingActionMenu.isOpened) {
            closeMenu()
            return true
        }

        return false
    }


    fun handlesTouch(event: MotionEvent): Boolean {
        if(floatingActionMenu.isOpened) { // if menu is opened and user clicked somewhere else in the view, close menu
            if(floatingActionMenu.isTouchInsideView(event) == false) {
                closeMenu()

                return true
            }
        }

        return false
    }

}