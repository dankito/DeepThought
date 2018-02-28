package net.dankito.deepthought.android.di

import net.dankito.synchronization.database.IEntityManager
import net.dankito.deepthought.di.CommonModule
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.InMemorySearchEngine
import net.dankito.synchronization.database.sync.ISyncManager
import net.dankito.service.synchronization.NoOpSyncManager
import net.dankito.service.synchronization.changeshandler.ISynchronizedChangesHandler
import net.dankito.service.synchronization.changeshandler.NoOpSynchronizedChangesHandler
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.language.ILanguageDetector


class UiTestCommonModule : CommonModule() {

    override fun provideSearchEngine(dataManager: DataManager, languageDetector: ILanguageDetector, threadPool: IThreadPool, osHelper: OsHelper, eventBus: IEventBus,
                                     itemService: ItemService, tagService: TagService, sourceService: SourceService, seriesService: SeriesService,
                                     readLaterArticleService: ReadLaterArticleService, fileService: FileService, localFileInfoService: LocalFileInfoService): ISearchEngine {
        return InMemorySearchEngine(dataManager.entityManager, threadPool)
    }

    override fun provideSyncManager(entityManager: IEntityManager, changesHandler: ISynchronizedChangesHandler, networkSettings: NetworkSettings): ISyncManager {
        return NoOpSyncManager()
    }

    override fun provideSynchronizedChangesHandler(entityManager: IEntityManager, changesNotifier: EntityChangedNotifier): ISynchronizedChangesHandler {
        return NoOpSynchronizedChangesHandler()
    }

}