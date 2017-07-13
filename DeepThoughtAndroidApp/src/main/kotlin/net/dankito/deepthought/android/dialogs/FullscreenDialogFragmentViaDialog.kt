package net.dankito.deepthought.android.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R


abstract class FullscreenDialogFragmentViaDialog : DialogFragment() {


    abstract fun getLayoutId(): Int

    abstract fun setupUI(rootView: View)


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, R.style.FullscreenDialog)

        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        dialog.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                 closeDialog()
                return@setOnKeyListener true
            }
            false
        }

        return dialog
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(getLayoutId(), container, false)

        setupToolbar(rootView)

        setupUI(rootView)

        return rootView
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


    protected fun closeDialog() {
        dialog.dismiss()
    }

}