package net.dankito.deepthought.service.clipboard


data class ClipboardContentOption(val title: String, private val action: (ClipboardContentOption) -> Unit) {

    companion object {
        val ActionDoneProgress = 100f

        val IndeterminateProgress = Float.MAX_VALUE
    }


    private val isExecutingListeners = mutableListOf<(progress: Float) -> Unit>()


    fun addIsExecutingListener(listener: (progress: Float) -> Unit) {
        isExecutingListeners.add(listener)
    }

    internal fun updateIsExecutingState(progress: Float) {
        isExecutingListeners.forEach { listener ->
            listener(progress)
        }
    }

    internal fun setIndeterminateProgressState() {
        updateIsExecutingState(IndeterminateProgress)
    }

    internal fun setActionDone() {
        updateIsExecutingState(ActionDoneProgress)
    }


    fun callAction() {
        action(this)
    }

}