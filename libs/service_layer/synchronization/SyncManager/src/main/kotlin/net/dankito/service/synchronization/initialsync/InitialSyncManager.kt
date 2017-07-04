package net.dankito.service.synchronization.initialsync


import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.User
import net.dankito.deepthought.model.enums.ExtensibleEnumeration
import net.dankito.service.synchronization.initialsync.model.DeepThoughtSyncInfo
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.service.synchronization.initialsync.model.UserSyncInfo
import net.dankito.utils.localization.Localization
import java.util.*


class InitialSyncManager(private var entityManager: IEntityManager, private var localization: Localization) {

    fun syncUserInformationWithRemoteOnes(localUser: User, remoteUser: UserSyncInfo) {
        localUser.universallyUniqueId = remoteUser.universallyUniqueId
        localUser.userName = remoteUser.userName
        localUser.firstName = remoteUser.firstName
        localUser.lastName = remoteUser.lastName
        // TODO: be aware password gets overwritten on one side during process

        entityManager.updateEntity(localUser)
    }


    fun syncLocalDatabaseIdsWithRemoteOnes(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo) {
        syncLocalDatabaseIdsWithRemoteOnes(localDeepThought, localDeepThought.localUser, remoteSyncInfo.deepThought, remoteSyncInfo.user)
    }

    fun syncLocalDatabaseIdsWithRemoteOnes(localDeepThought: DeepThought, localUser: User, remoteDeepThought: DeepThoughtSyncInfo, remoteUser: UserSyncInfo) {
        entityManager.deleteEntity(localDeepThought)
        entityManager.deleteEntity(localUser)

        localDeepThought.id = remoteDeepThought.id

        localUser.id = remoteUser.id

        // TODO
//        persistSynchronizedDevices(entityManager, user, remoteUser)

        entityManager.persistEntity(localUser)
        entityManager.persistEntity(localDeepThought)


        updateExtensibleEnumerations(localDeepThought, remoteDeepThought, entityManager)
    }

    private fun persistSynchronizedDevices(entityManager: IEntityManager, loggedOnUser: User, remoteUser: UserSyncInfo) {
        // TODO
    }

    private fun updateExtensibleEnumerations(localDeepThought: DeepThought, remoteDeepThought: DeepThoughtSyncInfo, entityManager: IEntityManager) {
        updateExtensibleEnumeration(localDeepThought.applicationLanguages, remoteDeepThought.applicationLanguageIds, entityManager)

        updateExtensibleEnumeration(localDeepThought.noteTypes, remoteDeepThought.noteTypeIds, entityManager)

        updateExtensibleEnumeration(localDeepThought.fileTypes, remoteDeepThought.fileTypeIds, entityManager)

        entityManager.updateEntity(localDeepThought)
    }

    private fun <T: ExtensibleEnumeration> updateExtensibleEnumeration(localExtensibleEnumerationEntities: MutableCollection<T>,
                                            remoteExtensibleEnumerationIds: Map<String, String>, entityManager: IEntityManager) {
        val backup = ArrayList<T>(localExtensibleEnumerationEntities)
        localExtensibleEnumerationEntities.clear() // to update EntityCollection's Ids

        for((key, entityId) in remoteExtensibleEnumerationIds) {
            for(enumerationEntity in backup) {
                if(key == enumerationEntity.nameResourceKey) {
                    updateExtensibleEnumeration(enumerationEntity, entityId, entityManager)
                    break
                }
            }
        }

        for (item in backup) {
            localExtensibleEnumerationEntities.add(item)
        }
    }

    private fun updateExtensibleEnumeration(enumerationEntity: ExtensibleEnumeration, id: String, entityManager: IEntityManager) {
        entityManager.deleteEntity(enumerationEntity)

        enumerationEntity.id = id

        entityManager.persistEntity(enumerationEntity)
    }


    fun shouldUseLocalUserName(localUser: User, remoteUser: UserSyncInfo): Boolean {
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
    fun shouldUseLocalDatabaseIds(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo): Boolean {
        return shouldUseLocalDatabaseIds(localDeepThought, localDeepThought.localUser, remoteSyncInfo.deepThought, remoteSyncInfo.user)
    }

    @Throws(IllegalStateException::class)
    fun shouldUseLocalDatabaseIds(localDeepThought: DeepThought, localUser: User,
                                  remoteDeepThought: DeepThoughtSyncInfo, remoteUser: UserSyncInfo): Boolean {
        val countSynchronizingDevices = localUser.synchronizedDevices.size

        if (countSynchronizingDevices > 0 && remoteUser.countSynchronizedDevices == 0) {
            return true
        }
        if (countSynchronizingDevices == 0 && remoteUser.countSynchronizedDevices > 0) {
            return false
        }
        if (countSynchronizingDevices > 0 && remoteUser.countSynchronizedDevices > 0) {
            if (isTheSameUser(localUser, remoteUser, localDeepThought, remoteDeepThought) == false) {
                // TODO: now we're in a Trap, this has to be urgently resolved:
                // Both devices have already synchronized their Database Ids with other Devices, so no matter which one we choose,
                // these already synchronized devices must update their Database Ids as well
                throw IllegalStateException(localization.getLocalizedString("alert.message.message.both.devices.already.synchronized.with.other.devices"))
            }
        }

        return true
    }

    private fun isTheSameUser(localUser: User, remoteUser: UserSyncInfo, localDeepThought: DeepThought, remoteDeepThought: DeepThoughtSyncInfo): Boolean {
        return localUser.id == remoteUser.id && localUser.universallyUniqueId == remoteUser.universallyUniqueId &&
                localDeepThought.id == remoteDeepThought.id
    }

}
