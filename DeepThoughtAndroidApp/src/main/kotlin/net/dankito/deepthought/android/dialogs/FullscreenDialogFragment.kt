package net.dankito.deepthought.android.dialogs

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.Toolbar
import android.view.*
import net.dankito.deepthought.android.R


abstract class FullscreenDialogFragment : DialogFragment() {

    private var hideStatusBar = false


    abstract fun getDialogTag(): String

    abstract fun getLayoutId(): Int

    abstract fun setupUI(rootView: View)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(getLayoutId(), container, false)

        if(hideStatusBar) {
            activity?.let { hideStatusBar(it) }
        }

        setupToolbar(rootView)

        setupUI(rootView)

        savedInstanceState?.let {
            restoreState(savedInstanceState)
        }

        return rootView
    }

    protected open fun restoreState(savedInstanceState: Bundle) {
        // may be overwritten in sub class
    }

    private fun hideStatusBar(activity: Activity) {
        hideStatusBar(activity.window)
    }

    private fun hideStatusBar(window: Window) {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            val decorView = window.getDecorView()

            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }

        return result
    }

    private fun setupToolbar(rootView: View) {
        (rootView.findViewById(R.id.toolbar) as? Toolbar)?.let { toolbar ->
            toolbar.title = ""

            toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
            toolbar.setNavigationOnClickListener { closeDialog() }

            customizeToolbar(rootView, toolbar)
        }
    }

    protected open fun customizeToolbar(rootView: View, toolbar: Toolbar) {
        // may be overwritten in sub classes
    }


    protected open fun closeDialog() {
        activity?.let { activity ->
            closeDialogOnUiThread(activity)
        }
    }

    protected open fun closeDialogOnUiThread(activity: FragmentActivity) {
        val fragmentManager = activity.supportFragmentManager

        val transaction = fragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)

        transaction.remove(this)

        transaction.commit()
    }


    open fun showInFullscreen(fragmentManager: FragmentManager, hideStatusBar: Boolean = false) {
        this.hideStatusBar = hideStatusBar

        val transaction = fragmentManager.beginTransaction()

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(android.R.id.content, this, getDialogTag())

        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialog)

        transaction.addToBackStack(null).commit()
    }

}