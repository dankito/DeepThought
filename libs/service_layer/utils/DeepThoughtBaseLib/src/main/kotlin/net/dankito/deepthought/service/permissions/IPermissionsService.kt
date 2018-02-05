package net.dankito.deepthought.service.permissions


interface IPermissionsService {

    fun hasPermissionToWriteFiles(): Boolean

    fun requestPermissionToWriteSynchronizedFiles(requestPermissionResult: (Boolean) -> Unit)

}