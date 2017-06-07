package net.dankito.webclient


class WebClientResponse(val isSuccessful: Boolean, val error: Exception? = null, val body: String? = null) {

}