package net.dankito.deepthought.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*


@MappedSuperclass
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator::class,
        property = "id")
open class BaseEntity : Serializable {


    @Column(name = TableConfig.BaseEntityIdColumnName)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String? = null

    @Column(name = TableConfig.BaseEntityCreatedOnColumnName, updatable = false)/*, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"*/
    @Temporal(TemporalType.TIMESTAMP)
    var createdOn: Date
        private set

    @Column(name = TableConfig.BaseEntityModifiedOnColumnName)/*, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"*/
    @Temporal(TemporalType.TIMESTAMP)
    var modifiedOn: Date
        private set

    @Version
    @Column(name = TableConfig.BaseEntityVersionColumnName, nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    var version: Long? = null
        private set

    @Column(name = TableConfig.BaseEntityDeletedColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    var deleted = false
        private set


    init {
        modifiedOn = Date()
        createdOn = modifiedOn // to ensure they are not null
    }


    open fun setDeleted() {
        deleted = true
    }


    @Transient
    fun isPersisted(): Boolean {
        return id != null
    }

    @PrePersist
    protected open fun prePersist() {
        createdOn = Date()
        modifiedOn = createdOn
        version = 1L
    }

    @PreUpdate
    protected open fun preUpdate() {
        modifiedOn = Date()
    }

    @PreRemove
    protected open fun preRemove() {
        modifiedOn = Date()
    }

    @PostLoad
    protected open fun postLoad() {
        // may notify lazyLoadedEntityMapped
    }

    @PostPersist
    protected open fun postPersist() {
        // may notify lazyLoadedEntityMapped
    }


}