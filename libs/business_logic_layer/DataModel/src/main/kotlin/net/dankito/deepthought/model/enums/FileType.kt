package net.dankito.deepthought.model.enums

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob

@Entity(name = TableConfig.FileTypeTableName)
class FileType : ExtensibleEnumeration {

    companion object {
        private const val serialVersionUID = -1765124075257854178L
    }


    @Column(name = TableConfig.FileTypeFolderNameColumnName)
    var folderName: String

    @Column(name = TableConfig.FileTypeIconColumnName)
    @Lob
    var icon: ByteArray? = null


    private constructor() : this("", "")

    constructor(name: String, folderName: String) : super(name) {
        this.folderName = folderName
    }

    constructor(nameResourceKey: String, folderName: String, isSystemValue: Boolean, sortOrder: Int) : super(nameResourceKey, isSystemValue, sortOrder) {
        this.folderName = folderName
    }

}
