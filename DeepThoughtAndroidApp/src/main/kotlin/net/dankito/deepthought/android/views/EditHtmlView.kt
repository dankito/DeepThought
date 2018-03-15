package net.dankito.deepthought.android.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_item_content.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.getColorFromResourceId
import net.dankito.richtexteditor.Color
import net.dankito.richtexteditor.android.AndroidIcon
import net.dankito.richtexteditor.android.RichTextEditor
import net.dankito.richtexteditor.android.command.*
import net.dankito.richtexteditor.android.util.StyleApplier
import net.dankito.richtexteditor.command.ToolbarCommandStyle


class EditHtmlView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private lateinit var editor: RichTextEditor

    private var setHtml: String? = null


    fun setupHtmlEditor(rootView: View) {
        editor = rootView.contentEditor
        editor.setEditorHeight(500) // don't know why but it's important to set a height (at least on some older Androids)

        editor.javaScriptExecutor.addLoadedListener {
            (context as? Activity)?.runOnUiThread { editorHasLoaded() }
        }

        setupEditorToolbar(rootView)

        val optionsBar = rootView.lytFullscreenWebViewOptionsBar
        optionsBar.showMarkSelectedTextButton(editor)
        editor.setEditorToolbarAndOptionsBar(rootView.lytEditorToolbar, optionsBar)
    }

    private fun editorHasLoaded() {
        editor.setEditorFontSize(18) // TODO: make settable in settings and then save to LocalSettings
        editor.setPadding(10)
    }

    private fun setupEditorToolbar(rootView: View) {
        val styleApplier = StyleApplier()
        styleApplier.applyCommandStyle(AndroidIcon(R.drawable.ic_arrow_back_white_48dp), ToolbarCommandStyle(), rootView.btnCancelEditingContent)
        styleApplier.applyCommandStyle(AndroidIcon(R.drawable.ic_check_white_48dp), ToolbarCommandStyle(), rootView.btnApplyEditedContent)

        val editorToolbar = rootView.editorToolbar
        editorToolbar.editor = editor

        editorToolbar.commandStyle.isActivatedColor = Color.fromArgb(context.getColorFromResourceId(R.color.colorPrimaryDark))

        editorToolbar.addCommand(BoldCommand())
        // temporarily removed some commands due to apply and cancel button not all commands fit onto the display // TODO: re-add
//        editorToolbar.addCommand(ItalicCommand())
//        editorToolbar.addCommand(UnderlineCommand())
        val switchBackgroundColorCommand = SwitchTextBackgroundColorOnOffCommand(icon = AndroidIcon(R.drawable.ic_marker_white_48dp), setOnColorToCurrentColor = false)
        switchBackgroundColorCommand.style.marginRightDp = ToolbarCommandStyle.GroupDefaultMarginRightDp
        editorToolbar.addCommand(switchBackgroundColorCommand)

        editorToolbar.addCommand(UndoCommand())
        val redoCommand = RedoCommand()
        redoCommand.style.marginRightDp = ToolbarCommandStyle.GroupDefaultMarginRightDp
        editorToolbar.addCommand(redoCommand)

        editorToolbar.addCommand(InsertBulletListCommand())
//        editorToolbar.addCommand(InsertNumberedListCommand())

        editorToolbar.addSearchView()
    }


    fun getHtml(): String {
        return editor.getHtml()
    }

    fun setHtml(html: String, baseUrl: String?) {
        setHtml = html

        editor.setHtml(html, baseUrl)
    }

    fun setHtmlChangedCallback(callback: (Boolean) -> Unit) {
        editor.javaScriptExecutor.addDidHtmlChangeListener(callback) // TODO: as we don't unset listener, won't there be a memory leak?
    }

}