package net.dankito.newsreader.summary

import net.dankito.webclient.IWebClient


class ImplementedArticleSummaryExtractors(val webClient: IWebClient) {

    private val extractors = LinkedHashMap<Class<out IImplementedArticleSummaryExtractor>, IImplementedArticleSummaryExtractor>()

    init {
        extractors.put(HeiseNewsArticleSummaryExtractor::class.java, HeiseNewsArticleSummaryExtractor(webClient))
        extractors.put(HeiseDeveloperArticleSummaryExtractor::class.java, HeiseDeveloperArticleSummaryExtractor(webClient))
        extractors.put(GuardianArticleSummaryExtractor::class.java, GuardianArticleSummaryExtractor(webClient))
        extractors.put(PostillonArticleSummaryExtractor::class.java, PostillonArticleSummaryExtractor(webClient))
    }


    fun getImplementedExtractors() = ArrayList<IImplementedArticleSummaryExtractor>(extractors.values)

}