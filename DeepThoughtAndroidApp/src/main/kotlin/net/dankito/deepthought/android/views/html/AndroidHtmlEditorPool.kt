package net.dankito.deepthought.android.views.html

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class AndroidHtmlEditorPool {

    companion object {
        private val MAX_HTML_EDITORS_TO_CACHE = 2

        private val log = LoggerFactory.getLogger(AndroidHtmlEditorPool::class.java)
    }


    private var availableHtmlEditors: Queue<AndroidHtmlEditor> = ConcurrentLinkedQueue()


    fun getHtmlEditor(context: Context, listener: IHtmlEditorListener): AndroidHtmlEditor {
        if(availableHtmlEditors.size > 0) {
            val editor = availableHtmlEditors.poll()
            editor.reInitHtmlEditor(listener)
            return editor
        }

        return createHtmlEditorOnUIThread(context, listener)
    }

    private fun createHtmlEditorOnUIThread(context: Context, listener: IHtmlEditorListener): AndroidHtmlEditor {
        log.info("Creating new HtmlEditor")

        return AndroidHtmlEditor(context, listener)
    }

    fun htmlEditorReleased(htmlEditor: AndroidHtmlEditor) {
        htmlEditor.resetInstanceVariables()

        if(htmlEditor.parent is ViewGroup) {
            (htmlEditor.parent as ViewGroup).removeView(htmlEditor)
        }

        // do not cache release HtmlEditors anymore, they are using to much Memory by time
        if(availableHtmlEditors.size < MAX_HTML_EDITORS_TO_CACHE && availableHtmlEditors.contains(htmlEditor) == false) {
            availableHtmlEditors.offer(htmlEditor)
        }

        log.info("Released HtmlEditor. There are now " + availableHtmlEditors.size + " available HtmlEditors in Pool.")
    }


    fun preloadHtmlEditors(activity: Activity, numberOfHtmlEditors: Int) {
        preloadHtmlEditorsSequentially(activity, numberOfHtmlEditors)
    }

    private fun preloadHtmlEditorsSequentially(activity: Activity, numberOfInstance: Int) {
        val htmlEditorReference = AtomicReference<AndroidHtmlEditor>()

        htmlEditorReference.set(createHtmlEditorOnUIThread(activity, object : IHtmlEditorListener {
            override fun editorHasLoaded(editor: HtmlEditorCommon) {
                htmlEditorPreloaded(activity, htmlEditorReference.get(), numberOfInstance)
            }

            override fun htmlCodeUpdated() {}

            override fun htmlCodeHasBeenReset() {}
        }))
    }

    private fun htmlEditorPreloaded(activity: Activity, htmlEditor: AndroidHtmlEditor, numberOfInstance: Int) {
        htmlEditorReleased(htmlEditor)

        if(numberOfInstance > 1) {
            activity.runOnUiThread {
                preloadHtmlEditorsSequentially(activity, numberOfInstance - 1) // load next html editor
            }
        }
    }


    fun cleanUp() {
        for(editor in availableHtmlEditors) {
            editor.cleanUp()
        }

        availableHtmlEditors.clear()
    }

}
