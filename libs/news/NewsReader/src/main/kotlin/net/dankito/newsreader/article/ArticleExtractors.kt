package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.recipes.ChefkochArticleExtractor
import net.dankito.newsreader.article.recipes.KochbarArticleExtractor
import net.dankito.newsreader.article.sites.WikipediaArticleExtractor
import net.dankito.newsreader.model.ArticleSummaryItem
import java.util.*


class ArticleExtractors(webClient: IWebClient) {

    val readability4JArticleExtractor = Readability4JArticleExtractor(webClient)

    private val implementedExtractors = LinkedHashMap<Class<out IArticleExtractor>, IArticleExtractor>()

    init {
        implementedExtractors.put(WikipediaArticleExtractor::class.java, WikipediaArticleExtractor(webClient))

        implementedExtractors.put(SueddeutscheArticleExtractor::class.java, SueddeutscheArticleExtractor(webClient))
        implementedExtractors.put(SueddeutscheMagazinArticleExtractor::class.java, SueddeutscheMagazinArticleExtractor(webClient))
        implementedExtractors.put(SueddeutscheJetztArticleExtractor::class.java, SueddeutscheJetztArticleExtractor(webClient))
        implementedExtractors.put(TazArticleExtractor::class.java, TazArticleExtractor(webClient))
        implementedExtractors.put(ZeitArticleExtractor::class.java, ZeitArticleExtractor(webClient))
        implementedExtractors.put(SpiegelArticleExtractor::class.java, SpiegelArticleExtractor(webClient))
        implementedExtractors.put(SpiegelMobileArticleExtractor::class.java, SpiegelMobileArticleExtractor(webClient))
        implementedExtractors.put(DerFreitagArticleExtractor::class.java, DerFreitagArticleExtractor(webClient))
        implementedExtractors.put(HeiseNewsArticleExtractor::class.java, HeiseNewsArticleExtractor(webClient))
        implementedExtractors.put(HeiseDeveloperArticleExtractor::class.java, HeiseDeveloperArticleExtractor(webClient))
        implementedExtractors.put(CtArticleExtractor::class.java, CtArticleExtractor(webClient))
        implementedExtractors.put(TelepolisArticleExtractor::class.java, TelepolisArticleExtractor(webClient))
        implementedExtractors.put(TechStageArticleExtractor::class.java, TechStageArticleExtractor(webClient))
        implementedExtractors.put(NetzPolitikOrgArticleExtractor::class.java, NetzPolitikOrgArticleExtractor(webClient))
        implementedExtractors.put(GuardianArticleExtractor::class.java, GuardianArticleExtractor(webClient))
        implementedExtractors.put(PostillonArticleExtractor::class.java, PostillonArticleExtractor(webClient))
        implementedExtractors.put(TagesschauArticleExtractor::class.java, TagesschauArticleExtractor(webClient))

        implementedExtractors.put(ChefkochArticleExtractor::class.java, ChefkochArticleExtractor(webClient))
        implementedExtractors.put(KochbarArticleExtractor::class.java, KochbarArticleExtractor(webClient))
    }


    fun getExtractorForItem(item: ArticleSummaryItem) : IArticleExtractor? {
        item.articleExtractorClass?.let { return getExtractorForClass(it) }

        return getExtractorForUrl(item.url)
    }

    fun getExtractorForUrl(url: String) : IArticleExtractor? {
        findImplementedExtractorThatCanExtractEntryFromUrl(url)?.let {
            return it
        }

        return defaultExtractor
    }

    private fun findImplementedExtractorThatCanExtractEntryFromUrl(url: String): IArticleExtractor? {
        implementedExtractors.forEach { (_, extractor) ->
            if(extractor.canExtractEntryFromUrl(url)) {
                return extractor
            }
        }

        return null
    }

    fun getExtractorForClass(extractorClass: Class<out IArticleExtractor>) = implementedExtractors[extractorClass]


    fun extractArticleAsync(url: String, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        getExtractorForUrl(url)?.extractArticleAsync(url, callback)
    }


    val defaultExtractor = readability4JArticleExtractor

    fun isDefaultExtractor(extractor: IArticleExtractor) = extractor == defaultExtractor

}