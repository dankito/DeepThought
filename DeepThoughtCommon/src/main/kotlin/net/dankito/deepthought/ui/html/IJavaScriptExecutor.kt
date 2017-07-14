package net.dankito.deepthought.ui.html


interface IJavaScriptExecutor {

    fun executeScript(javaScript: String)

    fun executeScript(javaScript: String, listener: ((result: Any) -> Unit)?)

    fun setJavaScriptMember(name: String, member: IJavaScriptBridge?)

}
