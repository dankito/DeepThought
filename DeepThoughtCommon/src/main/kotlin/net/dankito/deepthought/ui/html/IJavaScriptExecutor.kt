package net.dankito.deepthought.ui.html


interface IJavaScriptExecutor {

    fun executeScript(javaScript: String, listener: ((result: Any) -> Unit)? = null)

    fun setJavaScriptMember(name: String, member: IJavaScriptBridge?)

}
