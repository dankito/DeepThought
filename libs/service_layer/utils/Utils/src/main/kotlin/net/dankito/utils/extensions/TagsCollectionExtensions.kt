package net.dankito.utils.extensions

import net.dankito.deepthought.model.Tag


fun Iterable<Tag>.sorted(): List<Tag> {
    return this.filterNotNull().sortedByStrings { it.name }
}

fun Iterable<Tag>.toSortedString(): String {
    return this.sorted().joinToString { it.name }
}