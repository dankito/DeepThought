package net.dankito.utils.extensions

import net.dankito.deepthought.model.Tag
import net.dankito.utils.extensions.sortedByStrings
import java.text.Collator


class CollectionsExtensions {
    companion object {
        val collator = Collator.getInstance()

        init {
            collator.strength = Collator.IDENTICAL
        }
    }
}


fun Iterable<Tag>.sorted(): List<Tag> {
    return this.filterNotNull().sortedByStrings { it.name }
}

fun Iterable<Tag>.toSortedString(): String {
    return this.sorted().joinToString { it.name }
}


fun <T> Collection<T>.didCollectionChange(collectionToCompareTo: Collection<T>): Boolean {
    if(this.size != collectionToCompareTo.size) {
        return true
    }

    val copy = ArrayList(collectionToCompareTo)
    copy.removeAll(this)
    return copy.size > 0
}