package net.dankito.deepthought.android.di

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.InMemorySearchEngine
import net.dankito.service.synchronization.ISyncManager
import net.dankito.service.synchronization.NoOpSyncManager
import net.dankito.service.synchronization.changeshandler.ISynchronizedChangesHandler
import net.dankito.service.synchronization.changeshandler.NoOpSynchronizedChangesHandler
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.language.ILanguageDetector


class UiTestCommonModule : CommonModule() {

    override fun provideSearchEngine(dataManager: DataManager, languageDetector: ILanguageDetector, threadPool: IThreadPool, osHelper: OsHelper, eventBus: IEventBus, entryService: EntryService, tagService: TagService, referenceService: ReferenceService, seriesService: SeriesService, readLaterArticleService: ReadLaterArticleService): ISearchEngine {
        return InMemorySearchEngine(dataManager.entityManager, threadPool)
    }

    override fun provideSyncManager(entityManager: IEntityManager, changesHandler: ISynchronizedChangesHandler, networkSettings: INetworkSettings): ISyncManager {
        return NoOpSyncManager()
    }

    override fun provideSynchronizedChangesHandler(entityManager: IEntityManager, changesNotifier: EntityChangedNotifier): ISynchronizedChangesHandler {
        return NoOpSynchronizedChangesHandler()
    }

}