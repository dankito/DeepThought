package net.dankito.webclient.extractor


// TODO: find a better library (e.g. Utils lib)
data class AsyncResult<T>(val successful : Boolean, val error : Exception? = null, val result : T? = null)