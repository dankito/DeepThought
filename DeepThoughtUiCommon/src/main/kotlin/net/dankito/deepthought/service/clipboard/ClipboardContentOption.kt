package net.dankito.deepthought.service.clipboard


data class ClipboardContentOption(val title: String, private val action: (ClipboardContentOption) -> Unit) {

    companion object {
        val ActionStartProgress = 0f
        val ActionDoneProgress = 100f

        val IndeterminateProgress = Float.MAX_VALUE
    }


    private var currentProgress = ActionStartProgress

    private val isExecutingListeners = mutableListOf<(progress: Float) -> Unit>()


    val isExecuting: Boolean
        get() {
            return currentProgress == ClipboardContentOption.IndeterminateProgress ||
                    (currentProgress >= 0f && currentProgress < ClipboardContentOption.ActionDoneProgress)
        }

    val isDone: Boolean
        get() {
            return currentProgress != ClipboardContentOption.IndeterminateProgress &&
                    (currentProgress >= ClipboardContentOption.ActionDoneProgress || currentProgress < 0.0) // < 0.0 == error
        }

    val progressString: String
        get() {
            return  if(currentProgress >= 0.0 && currentProgress <= ClipboardContentOption.ActionDoneProgress) String.format("%.1f", currentProgress) + " %"
                    else ""
        }


    internal fun updateIsExecutingState(progress: Float) {
        currentProgress = progress

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



    fun addIsExecutingListener(listener: (progress: Float) -> Unit) {
        isExecutingListeners.add(listener)
    }

    fun callAction() {
        action(this)
    }

}