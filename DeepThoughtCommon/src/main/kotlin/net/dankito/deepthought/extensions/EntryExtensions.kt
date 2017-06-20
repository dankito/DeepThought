package net.dankito.deepthought.extensions

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler
import org.jsoup.Jsoup
import java.text.DateFormat
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)

private val previewCache = PreviewCache()


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

        if (preview.length < 200) {
            if (preview.length > 0) {
                preview += " "
            }

            preview += this.contentPlainText
        }

        previewCache.cacheEntryPreview(this, preview)
        return preview
    }

val Entry.referencePreview: String
    get() {
        reference?.let { reference ->
            var preview = reference.title

            var publisherAndDate = reference.series ?: ""

            reference.publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

            if(publisherAndDate.isNullOrBlank() == false) {
                preview = publisherAndDate.trim() + " - " + preview
            }

            return preview
        }

        return ""
    }

private fun getPlainTextForHtml(htmlString: String): String {
    return Jsoup.parseBodyFragment(htmlString).text()
}


class PreviewCache {

    private val abstractPlainTextCache = ConcurrentHashMap<Entry, String>()

    private val contentPlainTextCache = ConcurrentHashMap<Entry, String>()

    private val entryPreviewCache = ConcurrentHashMap<Entry, String>()


    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        CommonComponent.component.inject(this)

        eventBus.register(EventBusListener())
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


    private fun clearCacheForEntry(entry: Entry) {
        abstractPlainTextCache.remove(entry)
        contentPlainTextCache.remove(entry)
        entryPreviewCache.remove(entry)
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entryChanged: EntryChanged) {
            clearCacheForEntry(entryChanged.entity)
        }

    }

}