package net.dankito.webclient


data class WebClientResponse(val isSuccessful: Boolean, val error: Exception? = null, val body: String? = null)