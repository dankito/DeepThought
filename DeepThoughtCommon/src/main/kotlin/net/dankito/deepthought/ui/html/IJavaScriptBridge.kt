package net.dankito.deepthought.ui.html

/**
 * Public methods for communicating from CKEditor's JavaScript with Java code.
 */
interface IJavaScriptBridge {

    fun ckEditorLoaded()

    fun htmlChanged()

    fun htmlHasBeenReset()


    fun elementClicked(element: String, button: Int, clickX: Int, clickY: Int): Boolean

    fun elementDoubleClicked(element: String): Boolean

    fun beforeCommandExecution(commandName: String): Boolean

}
