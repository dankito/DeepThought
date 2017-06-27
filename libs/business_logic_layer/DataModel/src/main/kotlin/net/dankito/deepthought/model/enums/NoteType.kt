package net.dankito.deepthought.model.enums

import net.dankito.deepthought.model.Note
import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany

@Entity(name = TableConfig.NoteTypeTableName)
class NoteType : ExtensibleEnumeration {

    companion object {
        private const val serialVersionUID = 9127576069157935076L
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "type")/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/
    var notes: MutableSet<Note> = HashSet<Note>()
        private set


    private constructor() : this("")

    constructor(name: String) : super(name)

    constructor(nameResourceKey: String, isSystemValue: Boolean, sortOrder: Int) : super(nameResourceKey, isSystemValue, sortOrder)


    fun addNote(note: Note): Boolean {
        return notes.add(note)
    }

    fun removeNote(note: Note): Boolean {
        return notes.remove(note)
    }

}
