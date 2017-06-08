package net.dankito.deepthought.news.summary.config

import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.faviconextractor.Favicon
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.newsreader.feed.FeedArticleSummaryExtractor
import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.feed.RomeFeedReader
import net.dankito.newsreader.model.FeedArticleSummary
import net.dankito.newsreader.summary.ImplementedArticleSummaryExtractors
import net.dankito.serializer.ISerializer
import net.dankito.serializer.JacksonJsonSerializer
import org.slf4j.LoggerFactory


class ArticleSummaryExtractorConfigManager(private val webClient: IWebClient, val fileStorageService: IFileStorageService) {

    companion object {
        private val FILE_NAME = "ArticleSummaryExtractorConfigurations.json"

        private val log = LoggerFactory.getLogger(ArticleSummaryExtractorConfigManager::class.java)
    }


    private var configurations: MutableMap<String, ArticleSummaryExtractorConfig> = linkedMapOf()

    private val faviconExtractor = FaviconExtractor(webClient) // TODO: inject

    private val faviconComparator = FaviconComparator(webClient) // TODO: inject

    private val serializer: ISerializer = JacksonJsonSerializer() // TODO: inject

    private val listeners = mutableListOf<ConfigChangedListener>()


    init {
        readPersistedConfigs()

        initImplementedExtractors()

        initAddedExtractors()
    }

    private fun readPersistedConfigs() {
        try {
            // TODO: only temporarily store in file, use database later on
            fileStorageService.readFromTextFile(FILE_NAME)?.let { fileContent ->
                configurations = serializer.deserializeObject(fileContent, LinkedHashMap::class.java, String::class.java, ArticleSummaryExtractorConfig::class.java) as
                        LinkedHashMap<String, ArticleSummaryExtractorConfig>

                configurations.forEach { _, config ->
                    if(config.iconUrl == null) {
                        loadIconAsync(config)
                    }
                }
            }
        } catch(e: Exception) {
            log.error("Could not deserialize ArticleSummaryExtractorConfigs", e)
        }
    }

    private fun initImplementedExtractors() {
        ImplementedArticleSummaryExtractors(webClient).getImplementedExtractors().forEach { implementedExtractor ->
            var config = configurations.get(implementedExtractor.getBaseUrl())

            if (config == null) {
                config = ArticleSummaryExtractorConfig(implementedExtractor, implementedExtractor.getBaseUrl(), implementedExtractor.getName())
                addConfig(config)
            } else {
                config.extractor = implementedExtractor
            }
        }
    }

    private fun initAddedExtractors() {
        configurations.forEach { _, config ->
            if(config.extractor == null) {
                config.extractor = FeedArticleSummaryExtractor(config.url, createFeedReader())
            }
        }
    }

    private fun loadIconAsync(config: ArticleSummaryExtractorConfig) {
        loadIconAsync(config.url) { bestIconUrl ->
            bestIconUrl?.let {
                config.iconUrl = it
                saveConfig(config)
            }
        }
    }

    private fun loadIconAsync(url: String, callback: (String?) -> Unit)  {
        faviconExtractor.extractFaviconsAsync(url) {
            if(it.result != null) {
                callback(faviconComparator.getBestIcon(it.result as List<Favicon>, returnSquarishOneIfPossible = true)?.url)
            }
            else {
                callback(null)
            }
        }
    }


    fun getConfigs() : List<ArticleSummaryExtractorConfig> {
        return configurations.values.toList()
    }

    fun getConfig(id: String) : ArticleSummaryExtractorConfig? {
        return configurations[id]
    }


    fun addFeed(feedUrl: String, summary: FeedArticleSummary, callback: (ArticleSummaryExtractorConfig?) -> Unit) {
        val extractor = FeedArticleSummaryExtractor(feedUrl, createFeedReader())

        getIconForFeed(summary) {
            val config = ArticleSummaryExtractorConfig(extractor, feedUrl, summary.title ?: "", it)

            addConfig(config)

            callback(config)
        }
    }

    private fun createFeedReader(): IFeedReader {
        return RomeFeedReader()
    }

    private fun getIconForFeed(summary: FeedArticleSummary, callback: (iconUrl: String?) -> Unit) {
        summary.imageUrl?.let { iconUrl ->
            if(faviconComparator.doesFitSize(iconUrl, mustBeSquarish = true)) {
                return callback(iconUrl)
            }
        }

        val siteUrl = summary.siteUrl
        if(siteUrl != null) {
            loadIconAsync(siteUrl) { iconUrl ->
                callback(iconUrl)
            }
        }
        else {
            callback(null)
        }
    }

    private fun addConfig(config: ArticleSummaryExtractorConfig) {
        configurations.put(config.url, config)

        saveConfig(config)

        if(config.iconUrl == null) {
            loadIconAsync(config)
        }
    }

    private fun saveConfig(config: ArticleSummaryExtractorConfig) {
        try {
            val serializedConfigurations = serializer.serializeObject(configurations)

            fileStorageService.writeToTextFile(serializedConfigurations, FILE_NAME)
        } catch(e: Exception) {
            log.error("Could not write configurations to " + FILE_NAME, e)
        }

        callListeners(config)
    }


    fun addListener(listener: ConfigChangedListener) {
        listeners.add(listener)
        log.info("Added: Count listeners now: " + listeners.size)
    }

    fun removeListener(listener: ConfigChangedListener) {
        listeners.remove(listener)
        log.info("Removed: Count listeners now: " + listeners.size)
    }

    private fun callListeners(config: ArticleSummaryExtractorConfig) {
        listeners.forEach { it.configChanged(config) }
    }

}