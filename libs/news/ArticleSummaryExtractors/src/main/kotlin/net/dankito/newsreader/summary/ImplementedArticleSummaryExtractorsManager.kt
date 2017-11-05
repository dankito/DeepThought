package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient


class ImplementedArticleSummaryExtractorsManager(val webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {

    private val extractors = LinkedHashMap<Class<out IImplementedArticleSummaryExtractor>, IImplementedArticleSummaryExtractor>()

    init {
        extractors.put(SueddeutscheArticleSummaryExtractor::class.java, SueddeutscheArticleSummaryExtractor(webClient))
        extractors.put(HeiseNewsArticleSummaryExtractor::class.java, HeiseNewsArticleSummaryExtractor(webClient))
        extractors.put(HeiseDeveloperArticleSummaryExtractor::class.java, HeiseDeveloperArticleSummaryExtractor(webClient))
        extractors.put(TelepolisArticleSummaryExtractor::class.java, TelepolisArticleSummaryExtractor(webClient))
        extractors.put(NetzPolitikOrgArticleSummaryExtractor::class.java, NetzPolitikOrgArticleSummaryExtractor(webClient))
        extractors.put(DerFreitagArticleSummaryExtractor::class.java, DerFreitagArticleSummaryExtractor(webClient))
        extractors.put(ZeitArticleSummaryExtractor::class.java, ZeitArticleSummaryExtractor(webClient))
        extractors.put(GuardianArticleSummaryExtractor::class.java, GuardianArticleSummaryExtractor(webClient))
        extractors.put(PostillonArticleSummaryExtractor::class.java, PostillonArticleSummaryExtractor(webClient))
    }


    override fun getImplementedExtractors() = ArrayList<IImplementedArticleSummaryExtractor>(extractors.values)

}