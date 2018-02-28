package net.dankito.synchronization.database.sync

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.DeepThought
import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.model.SyncInfo
import net.dankito.util.localization.Localization


class DeepThoughtInitialSyncManager(entityManager: IEntityManager, localization: Localization) : InitialSyncManager(entityManager, localization) {

    fun syncUserDevices(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo) {
        syncUserDevices(localDeepThought.localDevice.id!!, localDeepThought.localUser, remoteSyncInfo, remoteSyncInfo.user)
    }


    fun syncLocalDatabaseIdsWithRemoteOnes(localDeepThought: DeepThought, remoteSyncInfo: SyncInfo) {
        // do not sync DeepThought as otherwise e.g. localDevice gets overwritten

        syncLocalDatabaseIdsWithRemoteOnes(localDeepThought.localUser, remoteSyncInfo)


        (remoteSyncInfo as? DeepThoughtSyncInfo)?.let {
            updateArticleSummaryExtractorConfigs(remoteSyncInfo.articleSummaryExtractorConfigs)

            entityManager.updateEntity(localDeepThought)
        }
    }


    private fun updateArticleSummaryExtractorConfigs(articleSummaryExtractorConfigs: List<ArticleSummaryExtractorConfig>) {
        val localConfigs = entityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        articleSummaryExtractorConfigs.forEach { remoteConfig ->
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

}
