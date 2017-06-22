package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.TagTableName)
open class Tag(
        @Column(name = TableConfig.TagNameColumnName)
        var name: String

): UserDataEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 1204202485407318615L
    }




    @Column(name = TableConfig.TagDescriptionColumnName)
    var description = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
    var entries: MutableList<Entry> = ArrayList() // TODO: don't expose a mutable list to the outside
        private set

    init {
        this.name = name
    }


    private constructor() : this("")


    fun hasEntries(): Boolean {
        return countEntries > 0
    }

    val countEntries: Int
        get() = entries.size

    internal fun addEntry(entry: Entry): Boolean {
        // i know this is not perfect as added Entry could have a smaller EntryIndex than already added ones
        entries.add(0, entry) // but sorting is not an option as with sorting all Entries would have to be loaded witch is bad on Tags with a lot of Entries

        return true
    }

    internal fun removeEntry(entry: Entry): Boolean {
        return entries.remove(entry)
    }


    val displayText: String
        @Transient
        get() = "$name ($countEntries)"

    override fun toString(): String {
        return displayText
    }

}
