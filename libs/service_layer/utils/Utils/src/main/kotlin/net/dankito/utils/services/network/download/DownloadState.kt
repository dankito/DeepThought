package net.dankito.utils.services.network.download


data class DownloadState(val finished: Boolean, val successful: Boolean = false, val progress: Float = -1f, val error: Exception? = null)