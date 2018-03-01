package net.dankito.synchronization.search.specific

import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.search.Search
import net.dankito.synchronization.search.SearchWithCollectionResult


class FilesSearch<TFile : FileLink>(searchTerm: String = Search.EmptySearchTerm,
                  val fileType: FileType = FileType.LocalOrRemoteFiles,
                  val searchUri: Boolean = true, val searchName: Boolean = true,
                  val searchMimeType: Boolean = true, val searchFileType: Boolean = true,
                  val searchDescription: Boolean = true, val searchSourceUri: Boolean = true,
                  completedListener: (List<TFile>) -> Unit) : SearchWithCollectionResult<TFile>(searchTerm, completedListener) {


    enum class FileType {
        LocalFilesOnly,
        RemoteFilesOnly,
        LocalOrRemoteFiles
    }

}
