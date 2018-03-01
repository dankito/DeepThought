package net.dankito.synchronization.files.persistence

import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.model.LocalFileInfo


class CouchbaseLocalFileRepository(private val entityManager: IEntityManager) : ILocalFileInfoRepository {

    override fun persist(localFileInfo: LocalFileInfo) {
        entityManager.persistEntity(localFileInfo)
    }

    override fun update(localFileInfo: LocalFileInfo) {
        entityManager.updateEntity(localFileInfo)
    }

    override fun delete(localFileInfo: LocalFileInfo) {
        entityManager.deleteEntity(localFileInfo)
    }

}