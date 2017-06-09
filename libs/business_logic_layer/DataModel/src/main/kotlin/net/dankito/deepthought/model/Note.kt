package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.NoteType
import javax.persistence.*


@Entity(name = TableConfig.NoteTableName)
data class Note(

        @Column(name = TableConfig.NoteNoteColumnName)
        @Lob
        var note: String

) : UserDataEntity() {


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.NoteNoteTypeJoinColumnName)
    var type: NoteType? = null
        set(type) {
            val previousValue = this.type

            if (this.type !== type) {
                this.type?.removeNote(this)

                this.type = type

                type?.addNote(this)
            }
        }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.NoteEntryJoinColumnName)
    var entry: Entry? = null
        internal set


    init {
        // TODO
//        this.type = NoteType.getDefaultNoteType()
    }

    constructor() : this("")

}
