package net.dankito.synchronization.search.specific


import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.synchronization.model.enums.FileSyncStatus
import net.dankito.synchronization.search.Search
import net.dankito.synchronization.search.SearchWithCollectionResult


class LocalFileInfoSearch(val fileId: String? = null, val hasSyncStatus: FileSyncStatus? = null, val doesNotHaveSyncStatus: FileSyncStatus? = null,
                          completedListener: (List<LocalFileInfo>) -> Unit) : SearchWithCollectionResult<LocalFileInfo>(Search.EmptySearchTerm, completedListener)
