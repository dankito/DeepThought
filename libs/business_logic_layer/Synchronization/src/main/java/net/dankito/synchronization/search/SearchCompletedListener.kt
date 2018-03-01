package net.dankito.synchronization.search


interface SearchCompletedListener<T> {

    fun completed(results: T)

}
