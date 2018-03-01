package net.dankito.synchronization.search

import net.dankito.synchronization.search.specific.FilesSearch
import net.dankito.synchronization.search.specific.LocalFileInfoSearch


interface ISearchEngine {

    fun searchFiles(search: FilesSearch)

    fun searchLocalFileInfo(search: LocalFileInfoSearch)

}
