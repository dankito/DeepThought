package net.dankito.deepthought.javafx.util

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*


class ThrowNoErrorOnMissingValuePropertyResourceBundle(reader: Reader) : PropertyResourceBundle(reader) {

    constructor(stream: InputStream) : this(InputStreamReader(stream))

    // copied from TornadoFX Messages.kt

    /**
     * Always return true, since we want to supply a default text message instead of throwing exception
     */
    override fun containsKey(key: String) = true

    /**
     * Lookup resource in this bundle. If no value, lookup in parent bundle if defined.
     * If we still have no value, return "[key]" instead of null.
     */
    override fun handleGetObject(key: String?): Any {
        var value = super.handleGetObject(key)
        if (value == null && parent != null)
            value = parent.getObject(key)

        return value ?: "[$key]"
    }

}