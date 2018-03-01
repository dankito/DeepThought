package net.dankito.synchronization.search

import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.search.specific.FilesSearch
import net.dankito.synchronization.search.specific.LocalFileInfoSearch


interface ISearchEngine<TFile : FileLink> {

    fun searchFiles(search: FilesSearch<TFile>)

    fun searchLocalFileInfo(search: LocalFileInfoSearch)

}
