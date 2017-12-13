package net.dankito.deepthought.ui.html


interface IHtmlEditorListener {

    fun editorHasLoaded(editor: HtmlEditorCommon)

    /**
     *
     *  Called each time Html in CKEditor has changed.
     * **
     *
     *Important: Do not execute any expensive functions in listener method.
     *
     * This method is called indirectly from JavaScript code. Doing expensive functions can slow down even machines with 8 (virtual) cores.**
     * @param updatedHtmlCode The updated Html code
     */
    fun htmlCodeUpdated()

    /**
     *
     *
     * Called if after changing HTML the Undo Button is so often pressed that all changes have been undone -> original set HTML has been restored.
     *
     */
    fun htmlCodeHasBeenReset()

}
