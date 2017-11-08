package net.dankito.newsreader.article

import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document


class CruxArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return null
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return true
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        val article = ArticleExtractor(url, document)
                .extractMetadata()
                .extractContent()
                .article()

        val content = mayAddPreviewImageToContent(article, article.document?.outerHtml() ?: "")

        val previewImageUrl = if(article.imageUrl != null) makeLinkAbsolute(article.imageUrl, url) else null // article.imageUrl may be null

        extractionResult.setExtractedContent(Item(content, article.description),
                Source(article.title, url, previewImageUrl = previewImageUrl))
    }

    private fun mayAddPreviewImageToContent(article: Article, content: String): String {
        if(content.isNullOrBlank() == false && article.imageUrl != null && content.contains(article.imageUrl) == false) {
            for(image in article.images) {
                if(article.imageUrl.contains(image.src)) { // image.src may is a relative url
                    if(content.contains(image.src) == false) { // again check if content does not already contain (relative) imageUrl
                        return "<figure>" + image.element.outerHtml() + "</figure>" + content
                    }

                    break
                }
            }

            return "<figure><img src=\"${article.imageUrl}\" alt=\"preview image\" /></figure>${content}"
        }

        return content
    }
}