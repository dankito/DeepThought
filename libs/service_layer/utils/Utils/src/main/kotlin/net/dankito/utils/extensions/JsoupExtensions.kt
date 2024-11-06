package net.dankito.utils.extensions

import org.jsoup.nodes.Node

fun Node.attrOrNull(attributeKey: String): String? =
    this.attr(attributeKey).takeUnless { it.isEmpty() }