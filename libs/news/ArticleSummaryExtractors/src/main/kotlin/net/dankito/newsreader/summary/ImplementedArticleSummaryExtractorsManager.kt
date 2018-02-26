package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient


class ImplementedArticleSummaryExtractorsManager(val webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {

    private val extractors = LinkedHashMap<Class<out IImplementedArticleSummaryExtractor>, IImplementedArticleSummaryExtractor>()

    init {
        extractors.put(SueddeutscheArticleSummaryExtractor::class.java, SueddeutscheArticleSummaryExtractor(webClient))
        extractors.put(ZeitArticleSummaryExtractor::class.java, ZeitArticleSummaryExtractor(webClient))
        extractors.put(SpiegelArticleSummaryExtractor::class.java, SpiegelArticleSummaryExtractor(webClient))
        extractors.put(TelepolisArticleSummaryExtractor::class.java, TelepolisArticleSummaryExtractor(webClient))
        extractors.put(NetzPolitikOrgArticleSummaryExtractor::class.java, NetzPolitikOrgArticleSummaryExtractor(webClient))
        extractors.put(DerFreitagArticleSummaryExtractor::class.java, DerFreitagArticleSummaryExtractor(webClient))
        extractors.put(HeiseNewsArticleSummaryExtractor::class.java, HeiseNewsArticleSummaryExtractor(webClient))
        extractors.put(HeiseDeveloperArticleSummaryExtractor::class.java, HeiseDeveloperArticleSummaryExtractor(webClient))
        extractors.put(GuardianArticleSummaryExtractor::class.java, GuardianArticleSummaryExtractor(webClient))
        extractors.put(PostillonArticleSummaryExtractor::class.java, PostillonArticleSummaryExtractor(webClient))
        extractors.put(LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor::class.java, LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor(webClient))
        extractors.put(LeMondeDiplomatiqueArticleSummaryExtractor::class.java, LeMondeDiplomatiqueArticleSummaryExtractor(webClient))
        extractors.put(AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor::class.java, AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor(webClient))
    }


    override fun getImplementedExtractors() = ArrayList<IImplementedArticleSummaryExtractor>(extractors.values)

}