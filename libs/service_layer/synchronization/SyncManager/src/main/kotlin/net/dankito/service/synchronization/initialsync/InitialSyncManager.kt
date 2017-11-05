package net.dankito.service.synchronization.initialsync


import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
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


    fun syncUserDevices(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo) {
        syncUserDevices(localDeepThought, localDeepThought.localUser, remoteSyncInfo.deepThought, remoteSyncInfo.user)
    }

    private fun syncUserDevices(localDeepThought: DeepThought, localUser: User, remoteDeepThought: DeepThoughtSyncInfo, remoteUser: UserSyncInfo) {
        addSynchronizedDevice(localUser, remoteDeepThought.localDeviceId)
        addSynchronizedDevice(localUser, localDeepThought.localDevice.id!!) // fix: so that remote also sees us as synchronized device

        remoteUser.synchronizedDevicesIds.forEach { addSynchronizedDevice(localUser, it) }

        remoteUser.ignoredDevicesIds.forEach { addIgnoredDevice(localUser, it) }

        entityManager.updateEntity(localUser)
    }

    private fun addSynchronizedDevice(user: User, deviceId: String) {
        entityManager.getEntityById(Device::class.java, deviceId)?.let { device ->
            if(user.containsSynchronizedDevice(device) == false) {
                user.addSynchronizedDevice(device)
            }
        }
    }

    private fun addIgnoredDevice(user: User, deviceId: String) {
        entityManager.getEntityById(Device::class.java, deviceId)?.let { device ->
            if(user.containsIgnoredDevice(device) == false) {
                user.addIgnoredDevice(device)
            }
        }
    }


    fun syncLocalDatabaseIdsWithRemoteOnes(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo) {
        // do not sync DeepThought as otherwise e.g. localDevice gets overwritten

        val localUser = localDeepThought.localUser

        entityManager.deleteEntity(localUser)

        localUser.id = remoteSyncInfo.user.id

        entityManager.persistEntity(localUser)


        updateExtensibleEnumerations(localDeepThought, remoteSyncInfo.deepThought, entityManager)

        updateArticleSummaryExtractorConfigs(remoteSyncInfo)


        entityManager.updateEntity(localDeepThought)
    }


    private fun updateExtensibleEnumerations(localDeepThought: DeepThought, remoteDeepThought: DeepThoughtSyncInfo, entityManager: IEntityManager) {
        updateExtensibleEnumeration(localDeepThought.noteTypes, remoteDeepThought.noteTypeIds, entityManager)

        updateExtensibleEnumeration(localDeepThought.fileTypes, remoteDeepThought.fileTypeIds, entityManager)
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


    private fun updateArticleSummaryExtractorConfigs(remoteSyncInfo: SyncInfo) {
        val localConfigs = entityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        remoteSyncInfo.articleSummaryExtractorConfigs.forEach { remoteConfig ->
            val localConfig = localConfigs.firstOrNull { it.url == remoteConfig.url }

            if(localConfig == null) {
                entityManager.persistEntity(remoteConfig)
            }
            else {
                entityManager.deleteEntity(localConfig)
                mergeArticleSummaryExtractorConfigs(localConfig, remoteConfig)
                entityManager.persistEntity(localConfig)
            }
        }

        sortArticleSummaryExtractorConfigFavorites()
    }

    private fun mergeArticleSummaryExtractorConfigs(localConfig: ArticleSummaryExtractorConfig, remoteConfig: ArticleSummaryExtractorConfig) {
        localConfig.id = remoteConfig.id

        if(remoteConfig.iconUrl != null) {
            localConfig.iconUrl = remoteConfig.iconUrl
        }

        if(remoteConfig.isFavorite) {
            localConfig.isFavorite = remoteConfig.isFavorite
            localConfig.favoriteIndex = remoteConfig.favoriteIndex
        }
    }

    private fun sortArticleSummaryExtractorConfigFavorites() {
        val favorites = entityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java).filter { it.isFavorite }.sortedBy { it.favoriteIndex }

        for(i in 0..favorites.size - 1) {
            val favorite = favorites[i]
            favorite.favoriteIndex = i
            entityManager.updateEntity(favorite)
        }
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
    private fun shouldUseLocalDatabaseIds(localDeepThought: DeepThought, localUser: User,
                                  remoteDeepThought: DeepThoughtSyncInfo, remoteUser: UserSyncInfo): Boolean {
        val localCountSynchronizingDevices = localUser.synchronizedDevices.size
        val remoteCountSynchronizingDevices = remoteUser.synchronizedDevicesIds.size

        if(localCountSynchronizingDevices > 0 && remoteCountSynchronizingDevices == 0) {
            return true
        }
        if(localCountSynchronizingDevices == 0 && remoteCountSynchronizingDevices > 0) {
            return false
        }
        if(localCountSynchronizingDevices > 0 && remoteCountSynchronizingDevices > 0) {
            if (isTheSameUser(localUser, remoteUser, localDeepThought, remoteDeepThought) == false) {
                // TODO: now we're in a Trap, this has to be urgently resolved:
                // Both devices have already synchronized their Database Ids with other Devices, so no matter which one we choose,
                // these already synchronized devices must update their Database Ids as well
                throw IllegalStateException(localization.getLocalizedString("alert.message.both.devices.already.synchronized.with.other.devices"))
            }
        }

        return true
    }

    private fun isTheSameUser(localUser: User, remoteUser: UserSyncInfo, localDeepThought: DeepThought, remoteDeepThought: DeepThoughtSyncInfo): Boolean {
        return localUser.id == remoteUser.id && localUser.universallyUniqueId == remoteUser.universallyUniqueId &&
                localDeepThought.id == remoteDeepThought.id
    }

}
