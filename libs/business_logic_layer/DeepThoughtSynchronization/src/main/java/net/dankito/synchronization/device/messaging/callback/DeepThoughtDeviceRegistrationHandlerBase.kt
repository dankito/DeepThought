package net.dankito.synchronization.device.messaging.callback

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.DeepThought
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.database.sync.DeepThoughtSyncInfo
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.model.SyncInfo
import net.dankito.synchronization.model.UserSyncInfo
import net.dankito.util.localization.Localization
import net.dankito.util.ui.dialog.IDialogService


abstract class DeepThoughtDeviceRegistrationHandlerBase(protected val deepThought: DeepThought, entityManager: IEntityManager, networkSettings: NetworkSettings,
                                                        protected val deepThoughtInitialSyncManager: DeepThoughtInitialSyncManager, dialogService: IDialogService,
                                                        localization: Localization)
    : DeviceRegistrationHandlerBase(entityManager, networkSettings, deepThoughtInitialSyncManager, dialogService, localization) {


    override fun createSyncInfo(useCallerDatabaseIds: Boolean?, useCallerUserName: Boolean?): SyncInfo {
        val userSyncInfo = UserSyncInfo(networkSettings.localUser)

        val articleSummaryExtractorConfigs = entityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        return DeepThoughtSyncInfo(deepThought.localDevice.id!!, userSyncInfo, useCallerDatabaseIds, useCallerUserName, articleSummaryExtractorConfigs)
    }


    override fun doInitialSynchronization(syncInfo: SyncInfo, syncDatabaseIds: Boolean, syncUserInfo: Boolean) {
        super.doInitialSynchronization(syncInfo, syncDatabaseIds, syncUserInfo)

        if(syncDatabaseIds) {
            deepThoughtInitialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(deepThought, syncInfo)
        }
    }

}