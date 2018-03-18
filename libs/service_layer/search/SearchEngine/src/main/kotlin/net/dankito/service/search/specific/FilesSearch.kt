package net.dankito.service.search.specific


import net.dankito.deepthought.model.FileLink
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class FilesSearch(searchTerm: String = Search.EmptySearchTerm,
                  val fileType: FileType = FileType.LocalOrRemoteFiles,
                  val searchUri: Boolean = true, val searchName: Boolean = true,
                  val searchMimeType: Boolean = true, val searchFileType: Boolean = true,
                  val searchDescription: Boolean = true, val searchSourceUri: Boolean = true,
                  completedListener: (List<FileLink>) -> Unit) : SearchWithCollectionResult<FileLink>(searchTerm, completedListener) {


    enum class FileType {
        LocalFilesOnly,
        RemoteFilesOnly,
        LocalOrRemoteFiles
    }

}
