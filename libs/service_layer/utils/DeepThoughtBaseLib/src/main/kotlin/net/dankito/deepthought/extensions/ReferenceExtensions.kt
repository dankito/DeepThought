package net.dankito.deepthought.extensions

import net.dankito.deepthought.di.BaseComponent
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

        if(subTitle.isNotBlank()) {
            preview = subTitle + ": " + preview
        }

        var publisherAndDate = series ?: ""

        publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " " + preview
        }

        previewCache.cacheReferencePreview(this, preview)
        return preview
    }


class ReferencePreviewCache {

    private val referencePreviewCache = ConcurrentHashMap<Reference, String>()


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        BaseComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }


    fun getCachedReferencePreview(reference: Reference): String? {
        return referencePreviewCache[reference]
    }

    fun cacheReferencePreview(reference: Reference, referencePreview: String) {
        referencePreviewCache.put(reference, referencePreview)
    }


    private fun clearCacheForReference(reference: Reference) {
        referencePreviewCache.remove(reference)
    }


    inner class EventBusListener {

        @Handler()
        fun referenceChanged(referenceChanged: ReferenceChanged) {
            clearCacheForReference(referenceChanged.entity)
        }

    }

}

