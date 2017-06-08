package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.serializer.ISerializer
import net.dankito.serializer.JacksonJsonSerializer
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
    fun provideFaviconExtractor(webClient: IWebClient) : FaviconExtractor {
        return FaviconExtractor(webClient)
    }

    @Provides
    @Singleton
    fun provideFaviconComparator(webClient: IWebClient) : FaviconComparator {
        return FaviconComparator(webClient)
    }

}