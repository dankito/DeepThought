package net.dankito.synchronization.service.permissions


class JavaPermissionsService : IPermissionsService {

    override fun hasPermissionToWriteFiles(): Boolean {
        return true // on Java we have the permission to write files by default
    }

    override fun requestPermissionToWriteSynchronizedFiles(requestPermissionResult: (Boolean) -> Unit) {
        requestPermissionResult(true)
    }

}