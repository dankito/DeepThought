package net.dankito.deepthought.data

import net.dankito.deepthought.model.FileLink
import net.dankito.service.data.FileService
import net.dankito.service.data.LocalFileInfoService
import net.dankito.utils.IThreadPool


class FilePersister(private val fileService: FileService, private val localFileInfoService: LocalFileInfoService, private val threadPool: IThreadPool) {


    fun saveFileAsync(file: FileLink, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveFile(file))
        }
    }

    fun saveFile(file: FileLink, doChangesAffectDependentEntities: Boolean = true): Boolean {
        val localFileInfo = file.localFileInfo

        if(localFileInfo != null && localFileInfo.isPersisted() == false) { // if localFileInfo has been newly created but not persisted yet
            localFileInfoService.persist(localFileInfo)
        }


        val wasFilePersisted = file.isPersisted()
        if(wasFilePersisted == false) {
            fileService.persist(file)
        }
        else {
            fileService.update(file, doChangesAffectDependentEntities)
        }


        if(localFileInfo != null && wasFilePersisted == false) { // file's id is now set, so update localFileInfo to store file's id with it
            localFileInfoService.update(localFileInfo)
        }


        return true
    }

}