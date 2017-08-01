package net.dankito.newsreader.summary


interface IImplementedArticleSummaryExtractor : IArticleSummaryExtractor {

    fun getName() : String

    fun getUrl() : String

}