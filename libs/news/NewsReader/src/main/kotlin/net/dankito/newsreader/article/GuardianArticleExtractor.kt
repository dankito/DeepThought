package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.Article
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*


class GuardianArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun parseHtmlToArticle(document: Document, url: String): Article? {
        document.body().select("#article").first()?.let { articleElement ->
            articleElement.select(".mobile-only").remove()

            articleElement.select(".content__headline").first()?.let { titleElement ->
                articleElement.select(".js-content-main-column").first()?.let { contentMainElement ->
                    return extractArticle(url, articleElement, titleElement, contentMainElement)
                }
            }
        }

        return null
    }

    private fun extractArticle(url: String, articleElement: Element, titleElement: Element, contentMainElement: Element): Article {
        val article = Article(url, titleElement.text(), extractContent(contentMainElement))

        article.publishingDate = extractPublishingDate(contentMainElement)

        contentMainElement.select(".media-primary").first()?.let { article.previewImageUrl = extractUrlFromFigureElement(it) }

        articleElement.select(".content__standfirst").first()?.let { article.abstract = it.text() }

        return article
    }

    private fun extractUrlFromFigureElement(figureElement: Element): String? {
        figureElement.select("img").first()?.let {
            if(it.hasAttr("src")) {
                return it.attr("src")
            }
        }

        figureElement.select("source").maxBy { it.attr("sizes") }?.let { return it.attr("srcset") }

        return null
    }

    private fun extractContent(bodyElement: Element): String {
        return bodyElement.children().filter { shouldFilterElement(it) == false }.joinToString(separator = "") {
            cleanHtml(it)
            it.outerHtml()
        }
    }

    private fun cleanHtml(contentElement: Element) {
        // removes article recommendations (aside), largely drawn social media buttons (.block-share) and not working expand image button (.inline-expand-image)
        contentElement.select("aside, .block-share, .inline-expand-image").remove()
        contentElement.select(".u-responsive-ratio").attr("style", "") // style="padding-bottom: 60.00%" makes a large margin at bottom of image
    }

    private fun shouldFilterElement(element: Element) : Boolean {
        return element.hasClass("mobile-only") || element.hasClass("content__meta-container") || element.hasClass("submeta")
    }

    private fun extractPublishingDate(contentMainElement: Element): Date? {
        contentMainElement.select("time.content__dateline-lm").first()?.let {
            return Date(it.attr("data-timestamp").toLong())
        }

        return null
    }

}