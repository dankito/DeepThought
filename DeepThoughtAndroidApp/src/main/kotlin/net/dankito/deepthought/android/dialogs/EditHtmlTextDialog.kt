package net.dankito.deepthought.android.dialogs

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_edit_html_text.*
import kotlinx.android.synthetic.main.dialog_edit_html_text.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.showKeyboard
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import net.dankito.richtexteditor.android.RichTextEditor
import net.dankito.richtexteditor.android.command.*


class EditHtmlTextDialog : FullscreenDialogFragment() {

    companion object {
        val TAG: String = javaClass.name

        private const val HTML_EXTRA_NAME = "HTML"

        private const val HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME = "HTML_TO_EDIT_LABEL_RESOURCE_ID"

        private const val HINT_LABEL_RESOURCE_ID_EXTRA_NAME = "HINT_LABEL_RESOURCE_ID"
    }


    private lateinit var editor: RichTextEditor

    private lateinit var mnApplyHtmlChanges: MenuItem

    private var htmlToSetOnStart: String? = null

    private var htmlToEditLabelResourceId: Int? = null

    private var hintLabelResourceId: Int? = null

    private var htmlChangedCallback: ((String) -> Unit)? = null


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

        hintLabelResourceId?.let { showHint(rootView.txtHint, it) }

        setupHtmlEditor(rootView)
    }

    private fun setTitle(toolbar: android.support.v7.widget.Toolbar, it: Int) {
        toolbar.title = getString(it)
    }

    private fun showHint(txtHint: TextView, hintLabelResourceId: Int) {
        txtHint.text = txtHint.context.getString(hintLabelResourceId)

        txtHint.visibility = View.VISIBLE
    }

    private fun setupHtmlEditor(rootView: View) {
        editor = rootView.editor
        editor.setEditorBackgroundColor(Color.WHITE)
        editor.setEditorHeight(500) // don't know why but it's important to set a height
        editor.setEditorFontSize(20) // TODO: make settable in settings and then save to LocalSettings
        editor.setPadding(10, 10, 10, 10)

        htmlToSetOnStart?.let {
            editor.setHtml(it)
        }

        editor.addHtmlChangedListener { setDidHtmlChange(true) } // TODO: determine if html really changed
        editor.postDelayed({
            editor.focusEditorAndShowKeyboard()
        }, 500)

        setupEditorToolbar(rootView)
    }

    private fun setupEditorToolbar(rootView: View) {
        val editorToolbar = rootView.editorToolbar
        editorToolbar.editor = editor

        editorToolbar.addCommand(BoldCommand())
        editorToolbar.addCommand(ItalicCommand())
        editorToolbar.addCommand(UnderlineCommand())
        editorToolbar.addCommand(SwitchTextBackgroundColorOnOffCommand())

        editorToolbar.addCommand(UndoCommand())
        editorToolbar.addCommand(RedoCommand())

        editorToolbar.addCommand(InsertBulletListCommand())
        editorToolbar.addCommand(InsertNumberedListCommand())
    }

    override fun onResume() {
        super.onResume()

        editor.postDelayed({
            editor.showKeyboard()
            editor.focusEditor()
        }, 200)
    }

    override fun onPause() {
        this.view?.hideKeyboard()

        super.onPause()
    }

    override fun onDestroy() {
        htmlChangedCallback = null

        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            htmlToEditLabelResourceId?.let { outState.putInt(HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME, it) }

            hintLabelResourceId?.let { outState.putInt(HINT_LABEL_RESOURCE_ID_EXTRA_NAME, it) }

            outState.putString(HTML_EXTRA_NAME, editor.getHtml())
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            savedInstanceState.getString(HTML_EXTRA_NAME)?.let { html ->
                editor.postDelayed({
                    editor.setHtml(html)
                }, 100)
            }

            val htmlToEditResourceId = savedInstanceState.getInt(HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME)
            if(htmlToEditResourceId > 0) {
                this.htmlToEditLabelResourceId = htmlToEditResourceId
                setTitle(toolbar, htmlToEditResourceId)
            }

            val hintLabelResourceId = savedInstanceState.getInt(HINT_LABEL_RESOURCE_ID_EXTRA_NAME)
            if(hintLabelResourceId > 0) {
                this.hintLabelResourceId = hintLabelResourceId
                showHint(txtHint, hintLabelResourceId)
            }
        }
    }

    fun restoreDialog(htmlToEdit: String, htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, htmlChangedCallback)

        // TODO: set if html changed
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


    fun showDialog(fragmentManager: FragmentManager, htmlToEdit: String, htmlToEditLabelResourceId: Int? = null, hintLabelResourceId: Int? = null, htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, htmlChangedCallback)

        this.htmlToEditLabelResourceId = htmlToEditLabelResourceId
        this.hintLabelResourceId = hintLabelResourceId

        showInFullscreen(fragmentManager, false)
    }

    private fun setupDialog(htmlToEdit: String, htmlChangedCallback: ((String) -> Unit)?) {
        this.htmlToSetOnStart = htmlToEdit

        this.htmlChangedCallback = htmlChangedCallback
    }

    private fun applyChangesAndCloseDialog() {
        htmlChangedCallback?.invoke(editor.getHtml())

        closeDialog()
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