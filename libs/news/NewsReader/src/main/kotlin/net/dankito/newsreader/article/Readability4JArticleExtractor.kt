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
            val content = mayAddPreviewImageToContent(extractionResult, articleContent)
            extractionResult.setExtractedContent(Item(content, article.excerpt ?: extractionResult.item.summary), null) // WebPageMetaDataExtractor should make a better job than Readability when it comes to extracting metadata
        }
    }

    private fun mayAddPreviewImageToContent(extractionResult: ItemExtractionResult, content: String): String {
        extractionResult.source?.previewImageUrl?.let { previewImageUrl ->
            if(content.contains("<img") == false && content.contains(previewImageUrl) == false) {
                return "<figure><img src=\"$previewImageUrl\" alt=\"preview image\" style=\"max-height:400px; width:auto;\" /></figure>${content}"
            }
        }

        return content
    }

}