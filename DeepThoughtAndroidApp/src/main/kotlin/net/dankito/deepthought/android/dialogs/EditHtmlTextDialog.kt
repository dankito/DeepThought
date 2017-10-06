package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.dialog_edit_html_text.*
import kotlinx.android.synthetic.main.dialog_edit_html_text.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.html.AndroidHtmlEditor
import net.dankito.deepthought.android.views.html.AndroidHtmlEditorPool
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class EditHtmlTextDialog : FullscreenDialogFragment() {

    companion object {
        val TAG: String = javaClass.name

        private const val HTML_EXTRA_NAME = "HTML"

        private const val HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME = "HTML_TO_EDIT_LABEL_RESOURCE_ID"
    }


    private lateinit var htmlEditor: AndroidHtmlEditor

    private lateinit var mnApplyHtmlChanges: MenuItem

    private var htmlToSetOnStart: String? = null

    private var htmlToEditLabelResourceId: Int? = null

    private var htmlChangedCallback: ((String) -> Unit)? = null


    @Inject
    protected lateinit var htmlEditorPool: AndroidHtmlEditorPool


    init {
        AppComponent.component.inject(this)
    }


    override fun getDialogTag() = TAG

    override fun getLayoutId() = R.layout.dialog_edit_html_text

    override fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_edit_html_text_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }
        mnApplyHtmlChanges = rootView.toolbar.menu.findItem(R.id.mnApplyHtmlChanges)

        htmlToEditLabelResourceId?.let { setTitle(rootView.toolbar, it) }

        setupHtmlEditor(rootView)
    }

    private fun setTitle(toolbar: android.support.v7.widget.Toolbar, it: Int) {
        toolbar.title = getString(it)
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

        htmlEditor.requestFocus()

        htmlToSetOnStart?.let {
            htmlEditor.setHtml(it, true)
        }
    }

    override fun onPause() {
        this.view?.hideKeyboard()

        super.onPause()
    }

    override fun onDestroy() {
        htmlEditorPool.htmlEditorReleased(htmlEditor)

        htmlChangedCallback = null

        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            htmlToEditLabelResourceId?.let { outState.putInt(HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME, it) }

            val countDownLatch = CountDownLatch(1)
            htmlEditor.getHtmlAsync { content ->
                outState.putString(HTML_EXTRA_NAME, content)
                countDownLatch.countDown()
            }
            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            savedInstanceState.getString(HTML_EXTRA_NAME)?.let { html ->
                htmlEditor.postDelayed({
                    htmlEditor.setHtml(html, true)
                }, 100)
            }

            val resourceId = savedInstanceState.getInt(HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME)
            if(resourceId > 0) {
                this.htmlToEditLabelResourceId = resourceId
                setTitle(toolbar, resourceId)
            }
        }
    }

    fun restoreDialog(htmlToEdit: String, htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, htmlChangedCallback)

        htmlEditor.postDelayed({
            htmlEditor.getHtmlAsync { currentHtml ->
                if(currentHtml != htmlToEdit) {
                    setDidHtmlChange(true)
                }
            }
        }, 300) // must be greater than the value of postDelayed in onViewStateRestored()
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


    fun showDialog(fragmentManager: FragmentManager, htmlToEdit: String, htmlToEditLabelResourceId: Int? = null, htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, htmlChangedCallback)
        this.htmlToEditLabelResourceId = htmlToEditLabelResourceId

        showInFullscreen(fragmentManager, false)
    }

    private fun setupDialog(htmlToEdit: String, htmlChangedCallback: ((String) -> Unit)?) {
        this.htmlToSetOnStart = htmlToEdit

        this.htmlChangedCallback = htmlChangedCallback
    }

    private fun applyChangesAndCloseDialog() {
        htmlEditor.getHtmlAsync { html ->
            htmlChangedCallback?.invoke(html)

            closeDialog()
        }
    }


    private val htmlEditorListener = object : IHtmlEditorListener {

        override fun editorHasLoaded(editor: HtmlEditorCommon) {
        }

        override fun htmlCodeUpdated() {
            setDidHtmlChange(true)
        }

        override fun htmlCodeHasBeenReset() {
            setDidHtmlChange(false)
        }

    }

    private fun setDidHtmlChange(didChange: Boolean) {
        activity?.runOnUiThread { mnApplyHtmlChanges.isVisible = didChange }
    }

}