package net.dankito.service.search

import java.util.concurrent.atomic.AtomicBoolean


abstract class Search<TResult : Any>(searchTerm: String, protected val completedListener: (TResult) -> Unit) {


    companion object {
        const val EmptySearchTerm = ""
    }


    lateinit var results: TResult


    var searchTerm: String
        protected set

    protected var interrupt = AtomicBoolean(false)

    var isCompleted = false
        protected set


    init {
        this.searchTerm = searchTerm
    }


    fun interrupt() {
        this.interrupt.set(true)
    }

    val isInterrupted: Boolean
        get() = interrupt.get()


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
