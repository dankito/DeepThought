package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.readability4j.Readability4J
import org.jsoup.nodes.Document


class Readability4JArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return null
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return true
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        val readability = Readability4J(url, document)
        val article = readability.parse()

        article.content?.let { articleContent ->
            extractionResult.setExtractedContent(Item(articleContent, article.excerpt ?: extractionResult.item.summary), null) // WebPageMetaDataExtractor should make a better job than Readability when it comes to extracting metadata
        }
    }

}