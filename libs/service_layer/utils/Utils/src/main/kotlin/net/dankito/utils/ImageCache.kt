package net.dankito.utils

import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.RequestParameters
import net.dankito.utils.web.client.ResponseType
import net.dankito.utils.web.client.WebClientResponse
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.net.URI


class ImageCache(private val webClient: IWebClient, private val serializer: ISerializer, private val fileStorageService: IFileStorageService) {

    companion object {
        private const val CACHE_FOLDER_NAME = "ImageCache"

        private const val CACHE_FILE_NAME = "ImageCache.json"

        private val log = LoggerFactory.getLogger(ImageCache::class.java)
    }


    private var imageCache: MutableMap<String, File> = hashMapOf()

    private val cacheFile: File


    init {
        cacheFile = fileStorageService.getFileInDataFolder(CACHE_FILE_NAME, CACHE_FOLDER_NAME)

        readImageCache()
    }


    private fun readImageCache() {
        try {
            fileStorageService.readFromTextFile(cacheFile)?.let { fileContent ->
                imageCache = serializer.deserializeObject(fileContent, HashMap::class.java, String::class.java, File::class.java) as
                        HashMap<String, File>
            }
        } catch(e: Exception) {
            if(e is FileNotFoundException == false) { // on first start-up file does not exist -> don't log error in this case
                log.error("Could not deserialize ImageCache", e)
            }
        }
    }

    private fun saveImageCache() {
        try {
            val serializedCache = serializer.serializeObject(imageCache)
            fileStorageService.writeToTextFile(serializedCache, cacheFile)
        } catch(e: Exception) {
            log.error("Could not save ImageCache", e)
        }
    }

    private fun addImageToCache(url: String, file: File) {
        imageCache.put(url, file)

        saveImageCache()
    }


    fun getCachedForRetrieveIconForUrlAsync(url: String, callback: (AsyncResult<File>) -> Unit) {
        try {
            val cachedImage = imageCache[url]

            if (cachedImage != null && cachedImage.exists()) {
                callback(AsyncResult(true, result = cachedImage))
            }
            else {
                retrieveAndCacheImage(url, callback)
            }
        } catch(e: Exception) {
            log.error("Could not retrieve image for url " + url, e)
            callback(AsyncResult(false, e))
        }
    }

    private fun retrieveAndCacheImage(url: String, callback: (AsyncResult<File>) -> Unit) {
        val file = getUniqueFilenameFromUrl(url)
        val fileStream = fileStorageService.createFileOutputStream(file)

        val parameters = RequestParameters(url)
        parameters.responseType = ResponseType.Bytes
        parameters.downloadProgressListener = { progress, downloadedChunk -> fileStream.write(downloadedChunk) }

        webClient.getAsync(parameters) { response ->
            fileStream.flush()
            fileStream.close()

            handleWebClientResponse(url, response, file, callback)
        }
    }

    private fun handleWebClientResponse(url: String, response: WebClientResponse, file: File, callback: (AsyncResult<File>) -> Unit) {
        if (response.isSuccessful) {
            addImageToCache(url, file)
            callback(AsyncResult(true, result = file))
        } else {
            callback(AsyncResult(false, Exception(response.body)))
        }
    }

    private fun getUniqueFilenameFromUrl(url: String): File {
        val uri = URI(url)

        return fileStorageService.getFileInDataFolder(uri.host + "_" + uri.path.replace('/', '_'), CACHE_FOLDER_NAME)
    }

}