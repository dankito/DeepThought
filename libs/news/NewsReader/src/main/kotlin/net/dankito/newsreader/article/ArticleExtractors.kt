package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.ArticleSummaryItem


class ArticleExtractors(webClient: IWebClient) {

    private val defaultExtractor = DefaultArticleExtractor(webClient)

    private val implementedExtractors = LinkedHashMap<Class<out IArticleExtractor>, IArticleExtractor>()

    init {
        implementedExtractors.put(SueddeutscheArticleExtractor::class.java, SueddeutscheArticleExtractor(webClient))
        implementedExtractors.put(SueddeutscheMagazinArticleExtractor::class.java, SueddeutscheMagazinArticleExtractor(webClient))
        implementedExtractors.put(HeiseNewsAndDeveloperArticleExtractor::class.java, HeiseNewsAndDeveloperArticleExtractor(webClient))
        implementedExtractors.put(GuardianArticleExtractor::class.java, GuardianArticleExtractor(webClient))
        implementedExtractors.put(PostillonArticleExtractor::class.java, PostillonArticleExtractor(webClient))
    }


    fun getExtractorForItem(item: ArticleSummaryItem) : IArticleExtractor? {
        item.articleExtractorClass?.let { return getExtractorForClass(it) }

        return getExtractorForUrl(item.url)
    }

    fun getExtractorForUrl(url: String) : IArticleExtractor? {
        // TODO: implement
        return defaultExtractor
    }

    fun getExtractorForClass(extractorClass: Class<out IArticleExtractor>) = implementedExtractors[extractorClass]

}