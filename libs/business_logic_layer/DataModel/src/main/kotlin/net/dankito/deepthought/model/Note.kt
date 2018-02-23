package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.*


@Entity(name = TableConfig.NoteTableName)
data class Note(

        @Column(name = TableConfig.NoteNoteColumnName)
        var note: String

) : BaseEntity() {


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.NoteItemJoinColumnName)
    var item: Item? = null
        internal set


    constructor() : this("")

}
