package net.dankito.deepthought.model.enums


import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.Column
import javax.persistence.MappedSuperclass


@MappedSuperclass
open class ExtensibleEnumeration : BaseEntity {

    // TODO: localize nameResourceKey

    companion object {
        private const  val serialVersionUID = -5370585730042175143L
    }


    @Column(name = TableConfig.ExtensibleEnumerationNameColumnName)
    var name: String = ""

    @Column(name = TableConfig.ExtensibleEnumerationNameResourceKeyColumnName)
    var nameResourceKey: String? = null
        protected set

    @Column(name = TableConfig.ExtensibleEnumerationDescriptionColumnName)
    var description: String = ""

    @Column(name = TableConfig.ExtensibleEnumerationSortOrderColumnName)
    var sortOrder = Integer.MAX_VALUE

    @Column(name = TableConfig.ExtensibleEnumerationIsSystemValueColumnName)
    var isSystemValue: Boolean = false
        protected set

    @Column(name = TableConfig.ExtensibleEnumerationIsDeletableColumnName)
    var isDeletable: Boolean = false
        protected set


    protected constructor() {
        this.isSystemValue = false
        this.isDeletable = true
    }

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(nameResourceKey: String, isSystemValue: Boolean, isDeletable: Boolean, sortOrder: Int) {
        this.nameResourceKey = nameResourceKey
        this.isSystemValue = isSystemValue
        this.isDeletable = isDeletable
        this.sortOrder = sortOrder
    }

}
