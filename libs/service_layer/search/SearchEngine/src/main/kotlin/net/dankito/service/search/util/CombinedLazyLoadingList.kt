package net.dankito.service.search.util

import java.util.*


class CombinedLazyLoadingList<T> : AbstractList<T> {

    private var combinedUnderlyingCollections: MutableList<Collection<T>> = ArrayList()


    constructor() {

    }

    constructor(vararg underlyingCollections: Collection<T>) {
        for (underlyingCollection in underlyingCollections)
            combinedUnderlyingCollections.add(underlyingCollection)
    }


    override val size: Int
        get() {
            var size = 0

            combinedUnderlyingCollections.forEach { size += it.size }

            return size
        }

    override fun get(index: Int): T? {
        var count = 0
        var underlyingCollectionForThisIndex: Collection<T>? = null

        for (underlyingCollection in combinedUnderlyingCollections) {
            if(underlyingCollection.size + count > index) {
                underlyingCollectionForThisIndex = underlyingCollection
                break
            }
            count += underlyingCollection.size
        }

        if(underlyingCollectionForThisIndex != null) {
            return getItemFromCollection(index - count, underlyingCollectionForThisIndex)
        }

        return null
    }

    private fun getItemFromCollection(index: Int, collection: Collection<T>): T? {
        if (collection is List<*>) {
            return (collection as List<T>)[index]
        }

        var i = 0
        val iterator = collection.iterator()

        while (iterator.hasNext()) {
            if(i == index) {
                return iterator.next()
            }

            iterator.next()
            i++
        }

        return null
    }

    fun setUnderlyingCollection(underlyingCollection: Collection<T>) {
        clear()

        combinedUnderlyingCollections.add(underlyingCollection)
    }

    override fun addAll(collection: Collection<T>): Boolean {
        return combinedUnderlyingCollections.add(collection)
    }

    override fun clear() {
        combinedUnderlyingCollections.clear()
    }
}
