package net.dankito.deepthought.android.service.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.utils.ImageCache
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class BitmapCache(private val imageCache: ImageCache) {

    private val bitmapsCache = ConcurrentHashMap<String, Bitmap>()


    fun getBitmapForUrlAsync(url: String, callback: (AsyncResult<Bitmap>) -> Unit) {
        val cachedBitmap = bitmapsCache[url]

        if(cachedBitmap != null) {
            callback(AsyncResult(true, result = cachedBitmap))
        }
        else {
            imageCache.getCachedForRetrieveIconForUrlAsync(url) { result ->
                result.result?.let {
                    val bitmap = createAndCacheBitmap(url, it)
                    callback(AsyncResult(true, result =  bitmap))
                }
                result.error?.let { callback(AsyncResult(false, it)) }
            }
        }
    }

    private fun createAndCacheBitmap(url: String, file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.path)

        bitmapsCache.put(url, bitmap)

        return bitmap
    }

    fun clear() {
        bitmapsCache.clear()
    }

}