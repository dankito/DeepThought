package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.feedaddressextractor.FeedAddressExtractor
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.feed.RomeFeedReader
import net.dankito.serializer.ISerializer
import net.dankito.serializer.JacksonJsonSerializer
import net.dankito.service.data.EntryService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.LuceneSearchEngine
import net.dankito.utils.IThreadPool
import net.dankito.utils.ImageCache
import net.dankito.utils.ThreadPool
import javax.inject.Singleton


@Module
class CommonModule {


    @Provides
    @Singleton
    fun provideWebClient() : IWebClient {
        return OkHttpWebClient()
    }

    @Provides
    @Singleton
    fun provideSerializer() : ISerializer {
        return JacksonJsonSerializer()
    }


    @Provides
    @Singleton
    fun provideThreadPool() : IThreadPool {
        return ThreadPool()
    }

    @Provides
    @Singleton
    fun provideImageCache(webClient: IWebClient, serializer: ISerializer, fileStorageService: IFileStorageService) : ImageCache {
        return ImageCache(webClient,serializer, fileStorageService)
    }


    @Provides
    @Singleton
    fun provideSearchEngine(dataManager: DataManager, threadPool: IThreadPool, entryService: EntryService) : ISearchEngine {
        return LuceneSearchEngine(dataManager, threadPool, entryService)
    }


    @Provides
    @Singleton
    fun provideFaviconExtractor(webClient: IWebClient) : FaviconExtractor {
        return FaviconExtractor(webClient)
    }

    @Provides
    @Singleton
    fun provideFaviconComparator(webClient: IWebClient) : FaviconComparator {
        return FaviconComparator(webClient)
    }


    @Provides
    @Singleton
    fun provideArticleSummaryExtractorConfigManager(webClient: IWebClient, fileStorageService: IFileStorageService) : ArticleSummaryExtractorConfigManager {
        return ArticleSummaryExtractorConfigManager(webClient, fileStorageService)
    }

    @Provides
    @Singleton
    fun provideArticleExtractors(webClient: IWebClient) : ArticleExtractors {
        return ArticleExtractors(webClient)
    }


    @Provides
    @Singleton
    fun provideFeedAddressExtractor(webClient: IWebClient) : FeedAddressExtractor {
        return FeedAddressExtractor(webClient)
    }

    @Provides
    @Singleton
    fun provideFeedReader() : IFeedReader {
        return RomeFeedReader()
    }

}