package net.dankito.utils.services.network.download

import java.io.File


interface IFileDownloader {

    fun downloadAsync(url: String, destination: File, callback: (DownloadState) -> Unit)

    fun download(url: String, destination: File, callback: (DownloadState) -> Unit)

}