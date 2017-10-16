package net.dankito.deepthought.android.dialogs

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.dialog_edit_html_text.*
import kotlinx.android.synthetic.main.dialog_edit_html_text.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.showKeyboard
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener


class EditHtmlTextDialog : FullscreenDialogFragment() {

    companion object {
        val TAG: String = javaClass.name

        private const val HTML_EXTRA_NAME = "HTML"

        private const val HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME = "HTML_TO_EDIT_LABEL_RESOURCE_ID"
    }


    private lateinit var editor: jp.wasabeef.richeditor.RichEditor

    private var currentTextBackgroundColor = Color.WHITE

    private lateinit var mnApplyHtmlChanges: MenuItem

    private var htmlToSetOnStart: String? = null

    private var htmlToEditLabelResourceId: Int? = null

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

        setupHtmlEditor(rootView)
    }

    private fun setTitle(toolbar: android.support.v7.widget.Toolbar, it: Int) {
        toolbar.title = getString(it)
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

        editor.setOnTextChangeListener { setDidHtmlChange(true) } // TODO: determine if html really changed

        setupEditorToolbar(rootView)
    }

    private fun setupEditorToolbar(rootView: View) {
        rootView.btnBold.setOnClickListener { editor.setBold() }

        rootView.btnItalic.setOnClickListener { editor.setItalic() }

        rootView.btnUnderline.setOnClickListener { editor.setUnderline() }

        rootView.btnBackgroundColor.setOnClickListener {
            currentTextBackgroundColor = if(currentTextBackgroundColor == Color.WHITE) Color.YELLOW else Color.WHITE
            editor.setTextBackgroundColor(currentTextBackgroundColor)
        }

        rootView.btnUndo.setOnClickListener { editor.undo() }

        rootView.btnRedo.setOnClickListener { editor.redo() }

        rootView.btnInsertBulletList.setOnClickListener { editor.setBullets() }

        rootView.btnInsertNumberedList.setOnClickListener { editor.setNumbers() }
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

            val resourceId = savedInstanceState.getInt(HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME)
            if(resourceId > 0) {
                this.htmlToEditLabelResourceId = resourceId
                setTitle(toolbar, resourceId)
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