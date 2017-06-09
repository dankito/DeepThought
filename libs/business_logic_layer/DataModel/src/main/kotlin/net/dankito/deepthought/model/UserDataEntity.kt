package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig

import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.MappedSuperclass
import javax.persistence.OneToOne


@MappedSuperclass
open class UserDataEntity : BaseEntity() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableConfig.UserDataEntityCreatedByJoinColumnName)
    var createdBy: User? = null
        protected set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableConfig.UserDataEntityModifiedByJoinColumnName)
    var modifiedBy: User? = null
        protected set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableConfig.UserDataEntityDeletedByJoinColumnName)
    var deletedBy: User? = null
        protected set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableConfig.UserDataEntityOwnerJoinColumnName)
    var owner: User? = null


    override fun prePersist() {
        super.prePersist()

//        createdBy = Application.getLoggedOnUser()
//        modifiedBy = Application.getLoggedOnUser()
//
//        if (owner == null) {
//            // if already set, don't overwrite
//            owner = Application.getLoggedOnUser()
//        }
    }

    override fun preUpdate() {
        super.preUpdate()

//        if (Application.getLoggedOnUser() != null) {
//            modifiedBy = Application.getLoggedOnUser()
//        }
    }

    override fun setDeleted() {
        super.setDeleted()

//        deletedBy = Application.getLoggedOnUser()
    }

}
