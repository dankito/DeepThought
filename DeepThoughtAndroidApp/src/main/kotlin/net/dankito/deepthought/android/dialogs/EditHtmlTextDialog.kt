package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.dialog_edit_html_text.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.views.html.AndroidHtmlEditor
import net.dankito.deepthought.android.views.html.AndroidHtmlEditorPool
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.schedule


class EditHtmlTextDialog : FullscreenDialogFragment() {

    companion object {
        private const val HTML_INTENT_EXTRA_NAME = "HTML"
    }


    private lateinit var htmlEditor: AndroidHtmlEditor

    private var htmlToSetOnStart: String? = null

    private var htmlChangedCallback: ((String) -> Unit)? = null


    @Inject
    protected lateinit var htmlEditorPool: AndroidHtmlEditorPool


    init {
        AppComponent.component.inject(this)
    }


    override fun getLayoutId() = R.layout.dialog_edit_html_text

    override fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_edit_html_text_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }

        rootView.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        rootView.toolbar.setNavigationOnClickListener { closeDialog() }

        setupHtmlEditor(rootView)
    }

    private fun setupHtmlEditor(rootView: View) {
        htmlEditor = htmlEditorPool.getHtmlEditor(rootView.context, htmlEditorListener)

        rootView.lytHtmlEditor.addView(htmlEditor, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val contentEditorParams = htmlEditor.layoutParams as RelativeLayout.LayoutParams
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        htmlEditor.layoutParams = contentEditorParams

        htmlToSetOnStart?.let {
            htmlEditor.setHtml(it)
        }
    }

    override fun onDestroy() {
        htmlEditorPool.htmlEditorReleased(htmlEditor)

        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            val countDownLatch = CountDownLatch(1)
            htmlEditor.getHtmlAsync { content ->
                outState.putString(HTML_INTENT_EXTRA_NAME, content)
                countDownLatch.countDown()
            }
            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }

    override fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(HTML_INTENT_EXTRA_NAME)?.let { content ->
            Timer().schedule(100L) { htmlEditor.setHtml(content) } // set delayed otherwise setHtml() from editEntry() wins
        }
    }


    private fun menuItemClicked(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnApplyHtmlChanges -> {
                applyChangesAndCloseDialog()
                return true
            }
        }

        return false
    }


    fun showDialog(fragmentManager: FragmentManager, htmlToEdit: String, htmlChangedCallback: ((String) -> Unit)?) {
        this.htmlChangedCallback = htmlChangedCallback
        this.htmlToSetOnStart = htmlToEdit

        showInFullscreen(fragmentManager, false)
    }

    private fun applyChangesAndCloseDialog() {
        htmlEditor.getHtmlAsync { html ->
            htmlChangedCallback?.invoke(html)

            closeDialog()
        }
    }

    override fun closeDialogOnUiThread(activity: FragmentActivity) {
        htmlChangedCallback = null

        this.view?.let { view ->
            val keyboard = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(view.windowToken, 0)
        }

        super.closeDialogOnUiThread(activity)
    }


    private val htmlEditorListener = object : IHtmlEditorListener {

        override fun editorHasLoaded(editor: HtmlEditorCommon) {
        }

        override fun htmlCodeUpdated() {
        }

        override fun htmlCodeHasBeenReset() {
        }

    }

}