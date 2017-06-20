package net.dankito.deepthought.extensions

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler
import java.text.DateFormat
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)

private val previewCache = ReferencePreviewCache()


val Reference?.preview: String
    get() {
        if(this == null) {
            return ""
        }

        previewCache.getCachedReferencePreview(this)?.let { return it }

        var preview = title

        var publisherAndDate = series ?: ""

        publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " - " + preview
        }

        previewCache.cacheReferencePreview(this, preview)
        return preview
    }


class ReferencePreviewCache {

    private val referencePreviewCache = ConcurrentHashMap<Reference, String>()


    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        CommonComponent.component.inject(this)

        eventBus.register(EventBusListener())
    }


    fun getCachedReferencePreview(reference: Reference): String? {
        return referencePreviewCache[reference]
    }

    fun cacheReferencePreview(referenc: Reference, referencePreview: String) {
        referencePreviewCache.put(referenc, referencePreview)
    }


    private fun clearCacheForReference(reference: Reference) {
        referencePreviewCache.remove(reference)
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(referenceChanged: ReferenceChanged) {
            clearCacheForReference(referenceChanged.entity)
        }

    }

}

