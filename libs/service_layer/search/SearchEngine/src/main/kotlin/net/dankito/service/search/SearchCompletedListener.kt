package net.dankito.service.search


interface SearchCompletedListener<T> {

    fun completed(results: T)

}
