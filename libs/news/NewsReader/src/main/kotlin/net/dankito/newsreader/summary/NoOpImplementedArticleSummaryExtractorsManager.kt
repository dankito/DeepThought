package net.dankito.newsreader.summary


class NoOpImplementedArticleSummaryExtractorsManager : IImplementedArticleSummaryExtractorsManager {

    override fun getImplementedExtractors(): List<IImplementedArticleSummaryExtractor> {
        return listOf()
    }

}