package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SueddeutscheArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        val SueddeutscheHeaderDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }


    override fun getName(): String? {
        return "SZ"
    }


    override fun extractArticle(url: String): EntryExtractionResult? {
        var siteUrl = url
        if(siteUrl.contains("?reduced=true")) {
            siteUrl = siteUrl.replace("?reduced=true", "")
        }

        return super.extractArticle(siteUrl)
    }

    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        if(isMultiPageArticle(document)) {
            return extractArticleWithPost(url, "article.singlePage=true")
        }

        document.body().select("#sitecontent").first()?.let { siteContent ->
            return extractArticle(siteContent, url)
        }

        document.body().select("article.gallery").first()?.let { galleryArticleElement ->
            return extractGalleryArticle(galleryArticleElement, url)
        }

        return null
    }

    private fun isMultiPageArticle(document: Document): Boolean {
        return document.body().getElementById("singlePageForm") != null
    }


    private fun extractArticle(siteContent: Element, siteUrl: String): EntryExtractionResult? {
        val reference = extractReference(siteContent, siteUrl)

        siteContent.select("#article-body").first()?.let { articleBody ->
            val abstract = articleBody.select(".entry-summary").first()?.html() ?: ""

            cleanArticleBody(articleBody)
            val content = loadLazyLoadingElementsAndGetContent(articleBody)

            val entry = Entry(content, abstract)

            siteContent.select(".topenrichment figure img").first()?.let { reference?.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            return EntryExtractionResult(entry, reference)
        }

        return null
    }

    private fun cleanArticleBody(articleBody: Element) {
        articleBody.select(".entry-summary, #article-sidebar-wrapper, .ad, .authors, .teaserable-layout").remove()
    }

    private fun loadLazyLoadingElementsAndGetContent(element: Element): String {
        extractInlineGalleries(element)

        super.loadLazyLoadingElements(element)

        return element.html()
    }

    private fun extractInlineGalleries(element: Element) {
        element.select("figure.gallery.inline").forEach { inlineGallery ->
            inlineGallery.select(".navigation").remove()

            inlineGallery.select("li").forEach { imageListElement ->
                imageListElement.remove()
                inlineGallery.append("<p>" + imageListElement.html() + "</p>")
            }
        }
    }


    private fun extractGalleryArticle(galleryArticleElement: Element, siteUrl: String): EntryExtractionResult? {
        val reference = extractReference(galleryArticleElement, siteUrl)

        val abstract = galleryArticleElement.select(".entry-summary").first()?.text() ?: ""

        galleryArticleElement.select("#article-body").first()?.let { articleBody ->
            val content = StringBuilder()
            readHtmlOfAllImagesInGallery(content, articleBody, siteUrl)

            articleBody.select(".offscreen").first()?.let { reference?.publishingDate = parseSueddeutscheDateString(it.text()) }

            return EntryExtractionResult(Entry(content.toString(), abstract), reference)
        }

        return null
    }

    private fun readHtmlOfAllImagesInGallery(imageHtml: StringBuilder, articleBody: Element, siteUrl: String) {
        articleBody.select("img").first()?.let {
            imageHtml.append(loadLazyLoadingElement(it).outerHtml())
        }

        articleBody.select(".caption").first()?.let { caption ->
            caption.select(".article-sidebar-wrapper, .date-copy").remove()
            imageHtml.append("<br/>" + caption.html())
        }

        getUrlOfNextImageInGallery(articleBody)?.let { nextImageUrl ->
            readHtmlOfAllImagesInGallery(imageHtml, nextImageUrl)
        }
    }

    private fun getUrlOfNextImageInGallery(articleBody: Element): String? {
        return articleBody.select("a.next").first()?.attr("href")
    }

    private fun readHtmlOfAllImagesInGallery(imageHtml: StringBuilder, nextImageUrl: String) {
        try {
            requestUrl(nextImageUrl).let { document ->
                document.body().select("#article-body > figure").first()?.let { articleBody ->
                    readHtmlOfAllImagesInGallery(imageHtml, articleBody, nextImageUrl)
                }
            }
        } catch (e: Exception) {
//            log.error("Could not extract html of next image in gallery from url " + nextImageUrl, e)
        }
    }


    private fun extractReference(articleElement: Element, url: String): Reference? {
        articleElement.select(".header").first()?.let { headerElement ->
            headerElement.select("h2").first()?.let { heading ->
                var subTitle = ""
                heading.select("strong").first()?.let {
                    subTitle = it.text()
                    it.remove() // remove element so that it's not as well part of title
                }

                val publishingDate = extractPublishingDate(headerElement)

                return Reference(url, heading.text(), publishingDate, getName(), subTitle)
            }
        }

        return null
    }

    private fun extractPublishingDate(headerElement: Element): Date? {
        headerElement.select("time").first()?.let {
            try {
                val dateString = it.attr("datetime")
                return parseSueddeutscheDateString(dateString)
            } catch(ignored: Exception) { }
        }

        return null
    }

    private fun parseSueddeutscheDateString(dateString: String) : Date? {
        try {
            return SueddeutscheHeaderDateFormat.parse(dateString)
        } catch(ignored: Exception) { }

        return null
    }

}