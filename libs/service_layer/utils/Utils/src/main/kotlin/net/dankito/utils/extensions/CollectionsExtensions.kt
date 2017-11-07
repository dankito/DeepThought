package net.dankito.utils.extensions

import net.dankito.deepthought.model.Tag
import java.text.Collator


class CollectionsExtensions {
    companion object {
        val collator = Collator.getInstance()

        init {
            collator.strength = Collator.IDENTICAL
        }
    }
}


/**
 * Standard sortedBy() function doesn't take characters like German Umlaute into consideration (so that e.g. Ärzte is ordered after Zucker)
 * -> use a Collator with at least strenght of Collator.SECONDARY
 */
inline fun <T> Iterable<T>.sortedByStrings(crossinline selector: (T) -> String): List<T> {
    return sortedWith(kotlin.Comparator { o1, o2 -> CollectionsExtensions.collator.compare(selector(o1), selector(o2)) })
}

fun Iterable<Tag>.sorted(): List<Tag> {
    return this.filterNotNull().sortedByStrings { it.name }
}