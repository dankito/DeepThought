package net.dankito.deepthought.javafx.util

import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import org.slf4j.LoggerFactory
import java.util.*


class LazyLoadingObservableList<T> : ObservableListBase<T>, ObservableList<T> {

    companion object {
        private val log = LoggerFactory.getLogger(LazyLoadingObservableList::class.java)
    }


    private var underlyingCollection: MutableCollection<T>? = null


    constructor() {
        setUnderlyingCollection(ArrayList<T>())
    }

    constructor(collection: MutableCollection<T>) {
        setUnderlyingCollection(collection)
    }


    override val size: Int
        get() {
            underlyingCollection?.let { underlyingCollection ->
                return underlyingCollection.size
            }

            return 0
        }

    override fun iterator(): MutableIterator<T> {
        return underlyingCollection!!.iterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        if(underlyingCollection is List<*>) {
            return (underlyingCollection as List<*>).listIterator(index) as MutableListIterator<T>
        }

        return super.listIterator(index)
    }

    override fun get(index: Int): T? {
        return getItemFromCollection(index, underlyingCollection)
    }

    private fun getItemFromCollection(index: Int, collection: Collection<T>?): T? {
        if (collection is List<*>) {
            return (collection as List<T>)[index]
        }

        collection?.let { collection ->
            var i = 0
            val iterator = collection.iterator()

            while(iterator.hasNext()) {
                if (i == index) {
                    return iterator.next()
                }

                iterator.next()
                i++
            }
        }

        return null
    }


    /**
     * Note that even thought Java method declaration says "Collection<T>" you have to provide a MutableCollection<T>, otherwise method will fail
     */
    override fun setAll(collection: Collection<T>?): Boolean {
        if(collection is MutableCollection<T>) {
            setUnderlyingCollection(collection)
            return true
        }
        else { // should actually never be the case
            return false
        }
    }

    fun setUnderlyingCollection(underlyingCollection: MutableCollection<T>?) {
        // simply make sure that collection is set (or better: endChange() ) is called on UI Thread (otherwise a IllegalStateException would be thrown)
        FXUtils.runOnUiThread { setUnderlyingCollectionOnUiThread(underlyingCollection) }
    }

    private fun setUnderlyingCollectionOnUiThread(underlyingCollection: MutableCollection<T>?) {
        beginChange()
        // this only makes removed items getting loaded from Database. And as ObservableList is also working without why implementing it?
        //    if(this.underlyingCollection instanceof List)
        //      nextRemove(0, (List<T>)this.underlyingCollection);
        //    else if(this.underlyingCollection != null) { // TODO: test
        //      int i = 0;
        //      for(T item : this.underlyingCollection) {
        //        nextRemove(i, item);
        //        i++;
        //      }
        //    }

        this.underlyingCollection = underlyingCollection

        try {
            if (size > 0) {
                nextAdd(0, size)
            } else {
                nextAdd(0, 0)
            }

            endChange()
        } catch (ex: Exception) {
            log.error("Could not set underlying collection", ex)
        }

    }

    override fun clear() {
        setUnderlyingCollection(ArrayList<T>())
    }

    override fun add(index: Int, element: T) {
        beginChange()

        if (underlyingCollection is MutableList<*>) {
            (underlyingCollection as MutableList<T>).add(index, element)
        }
        else {
            underlyingCollection?.let { it.add(element) }
        }

        nextAdd(index - 1, index)
        endChange()
    }

    override fun remove(element: T): Boolean {
        var result = false
        beginChange()

        try {
            underlyingCollection?.let { underlyingCollection ->
                result = underlyingCollection.remove(element as T)
            }

            //      nextRemove(index, element);
        } catch (ex: Exception) {

        } finally {
            endChange()
        }

        return result
    }

}
