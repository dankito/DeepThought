package net.dankito.synchronization.files.persistence

import net.dankito.synchronization.model.LocalFileInfo


interface ILocalFileInfoRepository {

    fun persist(localFileInfo: LocalFileInfo)

    fun update(localFileInfo: LocalFileInfo)

    fun delete(localFileInfo: LocalFileInfo)

}