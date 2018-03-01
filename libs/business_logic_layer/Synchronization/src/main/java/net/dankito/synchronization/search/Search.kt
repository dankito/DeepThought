package net.dankito.synchronization.search

import java.util.concurrent.atomic.AtomicBoolean


abstract class Search<TResult : Any>(val searchTerm: String, protected val completedListener: (TResult) -> Unit) {


    companion object {
        const val EmptySearchTerm = ""
    }


    lateinit var results: TResult

    protected var interrupt = AtomicBoolean(false)


    val isInterrupted: Boolean
        get() = interrupt.get()

    var errorOccurred = false
        protected set

    var error: Exception? = null
        protected set

    var isCompleted = false
        protected set


    fun interrupt() {
        this.interrupt.set(true)
    }


    fun errorOccurred(error: Exception) {
        this.errorOccurred = true
        this.error = error
    }

    fun fireSearchCompleted() {
        if (isInterrupted) { // do not call completedListener then
            return
        }

        isCompleted = true

        completedListener(results)
    }


    override fun toString(): String {
        return searchTerm
    }

}
