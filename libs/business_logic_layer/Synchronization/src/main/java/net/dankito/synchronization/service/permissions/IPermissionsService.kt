package net.dankito.synchronization.service.permissions


interface IPermissionsService {

    fun hasPermissionToWriteFiles(): Boolean

    fun requestPermissionToWriteSynchronizedFiles(requestPermissionResult: (Boolean) -> Unit)

}