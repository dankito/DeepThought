package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_edit_html_text.*
import kotlinx.android.synthetic.main.dialog_edit_html_text.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.extensions.getColorFromResourceId
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.richtexteditor.android.RichTextEditor
import net.dankito.richtexteditor.android.command.*
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import javax.inject.Inject


class EditHtmlTextDialog : FullscreenDialogFragment() {

    companion object {
        val TAG: String = javaClass.name

        private const val HTML_EXTRA_NAME = "HTML"

        private const val HTML_TO_EDIT_LABEL_RESOURCE_ID_EXTRA_NAME = "HTML_TO_EDIT_LABEL_RESOURCE_ID"

        private const val HINT_LABEL_RESOURCE_ID_EXTRA_NAME = "HINT_LABEL_RESOURCE_ID"
    }


    @Inject
    protected lateinit var dialogService: IDialogService


    private var setHtml: String? = null

    private var baseUrl: String? = null

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
        rootView.toolbar.setNavigationOnClickListener { askIfUnsavedChangesShouldBeSavedAndCloseDialog() }
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
        editor.setEditorHeight(500) // don't know why but it's important to set a height (at least on some older Androids)

        htmlToSetOnStart?.let {
            setHtml(it)
        }

        editor.addDidHtmlChangeListener { setDidHtmlChange(it) }

        editor.addLoadedListener {
            activity?.runOnUiThread { editorHasLoaded() }
        }

        setupEditorToolbar(rootView)
    }

    private fun editorHasLoaded() {
        editor.setEditorFontSize(18) // TODO: make settable in settings and then save to LocalSettings
        editor.setPadding(10)

        editor.focusEditorAndShowKeyboard()
        editor.postDelayed({ // older androids would like to have an extra invitation
            editor.focusEditorAndShowKeyboard()
        }, 500)
    }

    private fun setupEditorToolbar(rootView: View) {
        val editorToolbar = rootView.editorToolbar
        editorToolbar.editor = editor

        editorToolbar.commandStyle.isActivatedColor = context.getColorFromResourceId(R.color.colorPrimaryDark)

        editorToolbar.addCommand(BoldCommand())
        editorToolbar.addCommand(ItalicCommand())
        editorToolbar.addCommand(UnderlineCommand())
        val switchBackgroundColorCommand = SwitchTextBackgroundColorOnOffCommand()
        switchBackgroundColorCommand.style.marginRightDp = ToolbarCommandStyle.GroupDefaultMarginRightDp
        editorToolbar.addCommand(switchBackgroundColorCommand)

        editorToolbar.addCommand(UndoCommand())
        val redoCommand = RedoCommand()
        redoCommand.style.marginRightDp = ToolbarCommandStyle.GroupDefaultMarginRightDp
        editorToolbar.addCommand(redoCommand)

        editorToolbar.addCommand(InsertBulletListCommand())
        editorToolbar.addCommand(InsertNumberedListCommand())
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
                    setHtml(html)
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

    fun restoreDialog(htmlToEdit: String, baseUrl: String? = null, htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, baseUrl, htmlChangedCallback)

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


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(mnApplyHtmlChanges.isVisible) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_apply))
        dialogService.showConfirmationDialog(getString(R.string.dialog_edit_html_alert_message_html_contains_unsaved_changes), config = config) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                applyChangesAndCloseDialog()
            }
            else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                closeDialog()
            }
        }
    }


    fun showDialog(fragmentManager: FragmentManager, htmlToEdit: String, baseUrl: String? = null, htmlToEditLabelResourceId: Int? = null, hintLabelResourceId: Int? = null,
                   htmlChangedCallback: ((String) -> Unit)?) {
        setupDialog(htmlToEdit, baseUrl, htmlChangedCallback)

        this.htmlToEditLabelResourceId = htmlToEditLabelResourceId
        this.hintLabelResourceId = hintLabelResourceId

        showInFullscreen(fragmentManager, false)
    }

    private fun setupDialog(htmlToEdit: String, baseUrl: String? = null, htmlChangedCallback: ((String) -> Unit)?) {
        this.htmlToSetOnStart = htmlToEdit
        this.baseUrl = baseUrl

        this.htmlChangedCallback = htmlChangedCallback
    }

    private fun applyChangesAndCloseDialog() {
        htmlChangedCallback?.invoke(editor.getHtml())

        closeDialog()
    }


    private fun setHtml(html: String) {
        setHtml = html

        editor.setHtml(html, baseUrl)
    }

    private fun setDidHtmlChange(didChange: Boolean) {
        activity?.runOnUiThread { mnApplyHtmlChanges.isVisible = didChange }
    }

}