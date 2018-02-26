package net.dankito.deepthought.data

import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.service.data.FileService
import net.dankito.util.IThreadPool


class FilePersister(private val fileService: FileService, private val fileManager: FileManager, private val threadPool: IThreadPool) {


    fun saveFileAsync(file: FileLink, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveFile(file))
        }
    }

    fun saveFile(file: FileLink, doChangesAffectDependentEntities: Boolean = true): Boolean {
        val wasFilePersisted = file.isPersisted()
        if(wasFilePersisted == false) {
            fileService.persist(file)
        }
        else {
            fileService.update(file, doChangesAffectDependentEntities)
        }


        fileManager.saveLocalFileInfoForLocalFile(file)

        return true
    }

}