package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToMany


@Entity(name = TableConfig.EntriesGroupTableName)
data class EntriesGroup(

        @Column(name = TableConfig.EntriesGroupGroupNameColumnName)
        var groupName: String

) : BaseEntity() {


    companion object {
        private const val serialVersionUID = -1858952131697371548L
    }



    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "entryGroups")
    var entries: MutableSet<Entry> = HashSet()
        private set

    @Column(name = TableConfig.EntriesGroupNotesColumnName)
    var notes: String = ""


    private constructor() : this("")


    internal fun addEntryToGroup(entry: Entry): Boolean {
        return entries.add(entry)
    }

    internal fun removeEntryFromGroup(entry: Entry): Boolean {
        return entries.remove(entry)
    }

}
