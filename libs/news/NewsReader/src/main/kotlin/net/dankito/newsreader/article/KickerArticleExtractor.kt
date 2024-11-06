package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.extensions.attrOrNull
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document

class KickerArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName() = "Kicker"

    override fun canExtractItemFromUrl(url: String) = isHttpOrHttpsUrlFromHost(url, "www.kicker.de")


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().selectFirst("article")?.let { article ->
            val title = article.selectFirst("header h2")?.text() ?: ""
            val subTitle = article.selectFirst("header > p")?.text() ?: ""

            article.selectFirst(".kick__article__content")?.let { contentElement ->
                contentElement.selectFirst(".kick__article__content__child")?.let { content ->
                    val summary = contentElement.selectFirst(".kick__article__teaser")?.text() ?: ""
                    val previewImage = contentElement.selectFirst(".kick__article__picture--teaser, .kick__article__picture")
                        ?.selectFirst("img")?.attrOrNull("src")

                    extractionResult.setExtractedContent(Item(content.outerHtml()), Source(title, url, previewImageUrl = previewImage, subTitle = subTitle))
                }
            }
        }
    }

}