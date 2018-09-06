package net.dankito.deepthought.android.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_item_content.view.*
import net.dankito.deepthought.android.R
import net.dankito.richtexteditor.android.AndroidIcon
import net.dankito.richtexteditor.android.RichTextEditor
import net.dankito.richtexteditor.android.util.StyleApplier
import net.dankito.richtexteditor.command.ToolbarCommandStyle
import net.dankito.utils.Color
import net.dankito.utils.android.extensions.ColorExtensions
import net.dankito.utils.android.extensions.getColorFromResource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


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

        val primaryColorDark = context.getColorFromResource(R.color.colorPrimaryDark)
        val primaryColorDarkWithTransparency = ColorExtensions.setTransparency(primaryColorDark, ToolbarCommandStyle.GroupedViewsDefaultBackgroundTransparency)
        editorToolbar.commandStyle.isActivatedColor = Color.fromArgb(primaryColorDarkWithTransparency)
        editorToolbar.styleChanged(true) // isActivatedColor should also get applied to GroupedCommandView's toolbars

        editorToolbar.commandStyle.widthDp = 48
        editorToolbar.styleChanged() // but not widthDp, they should keep their default width

        editorToolbar.centerCommandsHorizontally()
    }


    fun getCurrentHtmlBlocking(): String {
        val retrievedHtml = AtomicReference<String>(editor.getCachedHtml()) // as a fallback use cached html
        val countDownLatch = CountDownLatch(1)

        editor.getCurrentHtmlAsync { currentHtml ->
            retrievedHtml.set(currentHtml)
            countDownLatch.countDown()
        }

        try { countDownLatch.await(10, TimeUnit.SECONDS) } catch (ignored: Exception) { } // don't block endlessly (10 seconds are already a lot of time)

        return retrievedHtml.get()
    }

    fun setHtml(html: String, baseUrl: String?) {
        setHtml = html

        editor.setHtml(html, baseUrl)
    }

    fun setHtmlChangedCallback(callback: (Boolean) -> Unit) {
        editor.javaScriptExecutor.addDidHtmlChangeListener(callback) // TODO: as we don't unset listener, won't there be a memory leak?
    }

}