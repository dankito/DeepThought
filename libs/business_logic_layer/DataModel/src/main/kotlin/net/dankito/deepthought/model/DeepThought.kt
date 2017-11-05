package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.model.enums.NoteType
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.DeepThoughtTableName)
data class DeepThought(

        @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
        @JoinColumn(name = TableConfig.DeepThoughtLocalUserJoinColumnName)
        var localUser: User,

        @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
        @JoinColumn(name = TableConfig.DeepThoughtLocalDeviceJoinColumnName)
        val localDevice: Device,

        @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
        @JoinColumn(name = TableConfig.DeepThoughtLocalSettingsJoinColumnName)
        var localSettings: LocalSettings

) : BaseEntity(), Serializable {


    companion object {
        private const val serialVersionUID = -3232937271770851228L
    }


    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var noteTypes: MutableSet<NoteType> = TreeSet<NoteType>()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var fileTypes: MutableSet<FileType> = TreeSet<FileType>()
        private set


    @Column(name = TableConfig.DeepThoughtNextItemIndexColumnName)
    var nextItemIndex = 0L
        private set


    private constructor() : this(User(), Device(), LocalSettings())


    fun addNoteType(noteType: NoteType): Boolean {
        return noteTypes.add(noteType)
    }

    fun removeNoteType(noteType: NoteType): Boolean {
        if (noteType.isSystemValue) {
            return false
        }

        return noteTypes.remove(noteType)
    }

    fun addFileType(fileType: FileType): Boolean {
        return fileTypes.add(fileType)
    }

    fun removeFileType(fileType: FileType): Boolean {
        if (fileType.isSystemValue) {
            return false
        }

        return fileTypes.remove(fileType)
    }


    fun increaseNextItemIndex(): Long {
        synchronized(this) {
            return ++nextItemIndex
        }
    }

}
