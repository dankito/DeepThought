package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToMany


@Entity(name = TableConfig.DeepThoughtFileLinkTableName)
class FileLink(uriString: String, name: String = "", isLocalFile: Boolean = false) : net.dankito.synchronization.model.FileLink(uriString, name, isLocalFile) {

    companion object {
        private const val serialVersionUID = -7508656557829870723L
    }


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var itemsAttachedTo: MutableSet<Item> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var sourcesAttachedTo: MutableSet<Source> = HashSet()
        private set


    internal constructor() : this("")


    val isAttachedToItems: Boolean
        get() = itemsAttachedTo.size > 0

    internal fun addAsAttachmentToItem(item: Item): Boolean {
        return itemsAttachedTo.add(item)
    }

    internal fun removeAsAttachmentFromItem(item: Item): Boolean {
        return itemsAttachedTo.remove(item)
    }


    val isAttachedToSource: Boolean
        get() = sourcesAttachedTo.size > 0

    internal fun addAsAttachmentToSource(source: Source): Boolean {
        return sourcesAttachedTo.add(source)
    }

    internal fun removeAsAttachmentFromSource(source: Source): Boolean {
        return sourcesAttachedTo.remove(source)
    }

}
