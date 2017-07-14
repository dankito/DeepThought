package net.dankito.deepthought.android.views.html

import android.content.Context
import android.view.ViewGroup
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

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

    fun preloadHtmlEditors(context: Context, numberOfHtmlEditors: Int) {
        val preloadedHtmlEditors = HashMap<Int, AndroidHtmlEditor>(numberOfHtmlEditors)

        for (i in 0..numberOfHtmlEditors - 1) {
            val instance = i
            val htmlEditor = getHtmlEditor(context, object : IHtmlEditorListener {
                override fun editorHasLoaded(editor: HtmlEditorCommon) {
                    // Editor is loaded now
                    preloadedHtmlEditors.remove(instance)?.let { htmlEditor ->
                        htmlEditorReleased(htmlEditor)
                    }
                }

                override fun htmlCodeUpdated() { }

                override fun htmlCodeHasBeenReset() { }
            })

            preloadedHtmlEditors.put(instance, htmlEditor)
        }
    }

    fun cleanUp() {
        for (editor in availableHtmlEditors) {
            editor.cleanUp()
        }

        availableHtmlEditors.clear()
    }

}
