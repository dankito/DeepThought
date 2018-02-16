package net.dankito.deepthought.service.clipboard


data class ClipboardContentOption(val title: String, private val action: (ClipboardContentOption) -> Unit) {

    private val isExecutingListeners = mutableListOf<(progress: Float) -> Unit>()


    fun addIsExecutingListener(listener: (progress: Float) -> Unit) {
        isExecutingListeners.add(listener)
    }

    internal fun updateIsExecutingState(progress: Float) {
        isExecutingListeners.forEach { listener ->
            listener(progress)
        }
    }


    fun callAction() {
        action(this)
    }

}