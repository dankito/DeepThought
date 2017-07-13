package net.dankito.deepthought.ui.html


interface IJavaScriptExecutor {

    fun executeScript(javaScript: String)

    fun executeScript(javaScript: String, listener: ExecuteJavaScriptResultListener?)

    fun setJavaScriptMember(name: String, member: IJavaScriptBridge?)

}
