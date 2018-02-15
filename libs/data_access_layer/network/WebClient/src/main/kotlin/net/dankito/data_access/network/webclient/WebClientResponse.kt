package net.dankito.data_access.network.webclient

import java.io.InputStream


data class WebClientResponse(val isSuccessful: Boolean,
                             val headers: Map<String, String>? = null,
                             val error: Exception? = null,
                             val body: String? = null, val responseStream: InputStream? = null)