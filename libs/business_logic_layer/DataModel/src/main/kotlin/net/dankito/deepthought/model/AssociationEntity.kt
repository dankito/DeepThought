package net.dankito.deepthought.model

import javax.persistence.MappedSuperclass

/**
 * Just a dummy class to mark Association Entities. AssociationEntities contrary to all other Entities really get deleted from Database instead just setting their deleted flag to true.
 */
@MappedSuperclass
class AssociationEntity : BaseEntity()
