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

            val maxContentLength = 300 - preview.length
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

        val preview = this.tags.joinToString { it.name }

        previewCache.cacheTagPreview(this, preview)
        return preview
    }


private fun getPlainTextForHtml(htmlString: String): String {
    return Jsoup.parseBodyFragment(htmlString).text()
}


class EntryPreviewCache {

    private val abstractPlainTextCache = ConcurrentHashMap<String, String>()

    private val contentPlainTextCache = ConcurrentHashMap<String, String>()

    private val entryPreviewCache = ConcurrentHashMap<String, String>()

    private val tagsPreviewCache = ConcurrentHashMap<String, String>()


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        BaseComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    fun getCachedAbstractPlainText(entry: Entry): String? {
        return abstractPlainTextCache[entry.id]
    }

    fun cacheAbstractPlainText(entry: Entry, abstractPlainText: String) {
        entry.id?.let { abstractPlainTextCache.put(it, abstractPlainText) }
    }


    fun getCachedContentPlainText(entry: Entry): String? {
        return contentPlainTextCache[entry.id]
    }

    fun cacheContentPlainText(entry: Entry, contentPlainText: String) {
        entry.id?.let { contentPlainTextCache.put(it, contentPlainText) }
    }


    fun getCachedEntryPreview(entry: Entry): String? {
        return entryPreviewCache[entry.id]
    }

    fun cacheEntryPreview(entry: Entry, entryPreview: String) {
        entry.id?.let { entryPreviewCache.put(it, entryPreview) }
    }


    fun getCachedTagsPreview(entry: Entry): String? {
        return tagsPreviewCache[entry.id]
    }

    fun cacheTagPreview(entry: Entry, tagsPreview: String) {
        entry.id?.let { tagsPreviewCache.put(it, tagsPreview) }
    }


    private fun clearCacheForEntry(entry: Entry) {
        abstractPlainTextCache.remove(entry.id)
        contentPlainTextCache.remove(entry.id)
        entryPreviewCache.remove(entry.id)
        tagsPreviewCache.remove(entry.id)
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entryChanged: EntryChanged) {
            clearCacheForEntry(entryChanged.entity)
        }

    }

}