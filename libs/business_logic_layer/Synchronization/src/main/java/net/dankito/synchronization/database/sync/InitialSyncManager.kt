package net.dankito.synchronization.database.sync

import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.SyncInfo
import net.dankito.synchronization.model.User
import net.dankito.synchronization.model.UserSyncInfo
import net.dankito.util.localization.Localization


open class InitialSyncManager(protected var entityManager: IEntityManager, protected var localization: Localization) {

    open fun syncUserInformationWithRemoteOnes(localUser: User, remoteUser: UserSyncInfo) {
        localUser.universallyUniqueId = remoteUser.universallyUniqueId
        localUser.userName = remoteUser.userName
        localUser.firstName = remoteUser.firstName
        localUser.lastName = remoteUser.lastName
        // TODO: be aware password gets overwritten on one side during process

        entityManager.updateEntity(localUser)
    }


    open fun syncUserDevices(localDeviceId: String, localUser: User, remoteSyncInfo: SyncInfo) {
        addSynchronizedDevice(localUser, remoteSyncInfo.localDeviceId)
        addSynchronizedDevice(localUser, localDeviceId) // fix: so that remote also sees us as synchronized device

        val remoteUser = remoteSyncInfo.user

        remoteUser.synchronizedDevicesIds.forEach { addSynchronizedDevice(localUser, it) }

        remoteUser.ignoredDevicesIds.forEach { addIgnoredDevice(localUser, it) }

        entityManager.updateEntity(localUser)
    }

    protected open fun addSynchronizedDevice(user: User, deviceId: String) {
        entityManager.getEntityById(Device::class.java, deviceId)?.let { device ->
            if(user.containsSynchronizedDevice(device) == false) {
                user.addSynchronizedDevice(device)
            }
        }
    }

    protected open fun addIgnoredDevice(user: User, deviceId: String) {
        entityManager.getEntityById(Device::class.java, deviceId)?.let { device ->
            if(user.containsIgnoredDevice(device) == false) {
                user.addIgnoredDevice(device)
            }
        }
    }


    open fun syncLocalDatabaseIdsWithRemoteOnes(localUser: User, remoteSyncInfo: SyncInfo) {
        // do not sync DeepThought as otherwise e.g. localDevice gets overwritten

        entityManager.deleteEntity(localUser)

        localUser.id = remoteSyncInfo.user.id

        entityManager.persistEntity(localUser)
    }


    open fun shouldUseLocalUserName(localUser: User, remoteUser: UserSyncInfo): Boolean {
        val localUserName = localUser.userName
        val remoteUserName = remoteUser.userName

        if(localUserName.isNotBlank() && remoteUserName.isBlank()) {
            return true
        }

        if(localUserName.isBlank() && remoteUserName.isNotBlank()) {
            return false
        }

        if(remoteUserName.contains("root") && localUserName.contains("root") == false) {
            return true
        }
        if(remoteUserName.contains("root") == false && localUserName.contains("root")) {
            return false
        }

        // TODO: add additional tests
        return true
    }

    @Throws(IllegalStateException::class)
    open fun shouldUseLocalDatabaseIds(localUser: User, remoteSyncInfo: SyncInfo): Boolean {
        return shouldUseLocalDatabaseIds(localUser, remoteSyncInfo.user)
    }

    @Throws(IllegalStateException::class)
    protected open fun shouldUseLocalDatabaseIds(localUser: User, remoteUser: UserSyncInfo): Boolean {
        val localCountSynchronizingDevices = localUser.synchronizedDevices.size
        val remoteCountSynchronizingDevices = remoteUser.synchronizedDevicesIds.size

        if(localCountSynchronizingDevices > 0 && remoteCountSynchronizingDevices == 0) {
            return true
        }
        if(localCountSynchronizingDevices == 0 && remoteCountSynchronizingDevices > 0) {
            return false
        }
        if(localCountSynchronizingDevices > 0 && remoteCountSynchronizingDevices > 0) {
            if(isTheSameUser(localUser, remoteUser) == false) {
                // TODO: now we're in a Trap, this has to be urgently resolved:
                // Both devices have already synchronized their Database Ids with other Devices, so no matter which one we choose,
                // these already synchronized devices must update their Database Ids as well
                throw IllegalStateException(localization.getLocalizedString("alert.message.both.devices.already.synchronized.with.other.devices"))
            }
        }

        return true
    }

    protected open fun isTheSameUser(localUser: User, remoteUser: UserSyncInfo): Boolean {
        return localUser.id == remoteUser.id && localUser.universallyUniqueId == remoteUser.universallyUniqueId
    }

}
