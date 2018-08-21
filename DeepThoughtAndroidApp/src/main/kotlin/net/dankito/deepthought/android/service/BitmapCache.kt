package net.dankito.deepthought.android.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import net.dankito.utils.AsyncResult
import net.dankito.utils.ImageCache
import java.io.File
import java.lang.Exception
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
                result.result?.let { file ->
                    val bitmap = createAndCacheBitmap(url, file)
                    if(bitmap != null) {
                        callback(AsyncResult(true, result = bitmap))
                    }
                    else {
                        callback(AsyncResult(false, Exception("Could not decode bitmap from file $file")))
                    }
                }
                result.error?.let { callback(AsyncResult(false, it)) }
            }
        }
    }

    private fun createAndCacheBitmap(url: String, file: File): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.path)

        if(bitmap != null) {
            bitmapsCache.put(url, bitmap)
        }

        return bitmap
    }


    fun clear() {
        bitmapsCache.clear()
    }

}