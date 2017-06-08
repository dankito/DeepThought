package net.dankito.data_access.network.webclient


data class WebClientResponse(val isSuccessful: Boolean, val error: Exception? = null, val body: String? = null)