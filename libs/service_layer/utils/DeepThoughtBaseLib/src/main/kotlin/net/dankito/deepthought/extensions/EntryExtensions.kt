package net.dankito.deepthought.extensions

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.Entry
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler
import org.jsoup.Jsoup
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


private val previewCache = EntryPreviewCache()


val Entry.abstractPlainText: String
    get() {
        previewCache.getCachedAbstractPlainText(this)?.let { return it }

        val plainText = getPlainTextForHtml(this.abstractString)
        previewCache.cacheAbstractPlainText(this, plainText)

        return plainText
    }

val Entry.contentPlainText: String
    get() {
        previewCache.getCachedContentPlainText(this)?.let { return it }

        val plainText = getPlainTextForHtml(this.content)
        previewCache.cacheContentPlainText(this, plainText)

        return plainText
    }

val Entry.entryPreview: String
    get() {
        previewCache.getCachedEntryPreview(this)?.let { return it }

        var preview = this.abstractPlainText

        if (preview.length < 300) {
            if (preview.isNotEmpty()) {
                preview += " "
            }

            var maxContentLength = 300 - preview.length
            if(maxContentLength > this.contentPlainText.length) {
                maxContentLength = this.contentPlainText.length
            }
            preview += this.contentPlainText.substring(0, maxContentLength)
        }

        previewCache.cacheEntryPreview(this, preview)
        return preview
    }


val Entry.referencePreview: String
    get() {
        return reference.preview
    }


val Entry.tagsPreview: String
    get() {
        previewCache.getCachedTagsPreview(this)?.let { return it }

        val preview = this.tags.sortedBy { it.name.toLowerCase() }.joinToString { it.name }

        previewCache.cacheTagPreview(this, preview)
        return preview
    }


private fun getPlainTextForHtml(htmlString: String): String {
    if(htmlString != null) { // to be on the safe side, happened sometimes
        return Jsoup.parseBodyFragment(htmlString).text()
    }

    return ""
}


class EntryPreviewCache {

    private val abstractPlainTextCache = ConcurrentHashMap<Entry, String>()

    private val contentPlainTextCache = ConcurrentHashMap<Entry, String>()

    private val entryPreviewCache = ConcurrentHashMap<Entry, String>()

    private val tagsPreviewCache = ConcurrentHashMap<Entry, String>()


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        BaseComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    fun getCachedAbstractPlainText(entry: Entry): String? {
        return abstractPlainTextCache[entry]
    }

    fun cacheAbstractPlainText(entry: Entry, abstractPlainText: String) {
        abstractPlainTextCache.put(entry, abstractPlainText)
    }


    fun getCachedContentPlainText(entry: Entry): String? {
        return contentPlainTextCache[entry]
    }

    fun cacheContentPlainText(entry: Entry, contentPlainText: String) {
        contentPlainTextCache.put(entry, contentPlainText)
    }


    fun getCachedEntryPreview(entry: Entry): String? {
        return entryPreviewCache[entry]
    }

    fun cacheEntryPreview(entry: Entry, entryPreview: String) {
        entryPreviewCache.put(entry, entryPreview)
    }


    fun getCachedTagsPreview(entry: Entry): String? {
        return tagsPreviewCache[entry]
    }

    fun cacheTagPreview(entry: Entry, tagsPreview: String) {
        tagsPreviewCache.put(entry, tagsPreview)
    }


    private fun clearCacheForEntry(entry: Entry) {
        abstractPlainTextCache.remove(entry)
        contentPlainTextCache.remove(entry)
        entryPreviewCache.remove(entry)
        tagsPreviewCache.remove(entry)
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entryChanged: EntryChanged) {
            clearCacheForEntry(entryChanged.entity)
        }

    }

}