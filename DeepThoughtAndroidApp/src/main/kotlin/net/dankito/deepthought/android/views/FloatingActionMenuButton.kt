package net.dankito.deepthought.android.views

import android.os.Bundle
import android.view.MotionEvent
import com.github.clans.fab.FloatingActionMenu


open class FloatingActionMenuButton(protected val floatingActionMenu: FloatingActionMenu) {

    companion object {
        private const val IS_OPENED_EXTRA_NAME = "IS_OPENED"
    }


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


    fun saveInstanceState(outState: Bundle?) {
        outState?.let {
            outState.putBoolean(IS_OPENED_EXTRA_NAME, floatingActionMenu.isOpened)
        }
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if(savedInstanceState.getBoolean(IS_OPENED_EXTRA_NAME, false)) {
                floatingActionMenu.open(false)
            }
        }
    }

}