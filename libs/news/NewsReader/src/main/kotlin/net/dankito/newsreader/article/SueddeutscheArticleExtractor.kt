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


    private var triedToResolveMultiPageArticle = false


    override fun getName(): String? {
        return "SZ"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http://www.sueddeutsche.de/") && url.length > "http://www.sueddeutsche.de/".length
    }


    override fun extractArticle(url: String): EntryExtractionResult? {
        var siteUrl = url
        if(siteUrl.contains("?reduced=true")) {
            siteUrl = siteUrl.replace("?reduced=true", "")
        }

        return super.extractArticle(siteUrl)
    }

    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        if(isMultiPageArticle(document) && triedToResolveMultiPageArticle == false) { // some multi page articles after fetching read all on one page still have the read all on  page link
            triedToResolveMultiPageArticle = true
            extractArticleWithPost(extractionResult, url, "article.singlePage=true") // -> extractArticleWithPost() would be called endlessly. that's what triedToResolveMultiPageArticle is there for to avoid this
            return
        }

        triedToResolveMultiPageArticle = false

        document.body().select("#sitecontent").first()?.let { siteContent ->
            extractArticle(extractionResult, siteContent, url)
            return
        }

        document.body().select("article.gallery").first()?.let { galleryArticleElement ->
            extractGalleryArticle(extractionResult, galleryArticleElement, url)
        }
    }

    private fun isMultiPageArticle(document: Document): Boolean {
        return document.body().getElementById("singlePageForm") != null
    }


    private fun extractArticle(extractionResult: EntryExtractionResult, siteContent: Element, siteUrl: String) {
        val reference = extractReference(siteContent, siteUrl)

        siteContent.select("#article-body").first()?.let { articleBody ->
            val abstract = articleBody.select(".entry-summary").first()?.html() ?: ""

            cleanArticleBody(articleBody)
            var content = loadLazyLoadingElementsAndGetContent(siteContent, articleBody)

            siteContent.select(".topenrichment figure img").first()?.let { previewImage ->
                val previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(previewImage, "src", siteUrl)
                reference?.previewImageUrl = previewImageUrl
                previewImage.attr("src", previewImageUrl)
                content = "<p>" + previewImage.outerHtml() + "</p>" + content
            }

            val entry = Entry(content, abstract)

            extractionResult.setExtractedContent(entry, reference)
        }
    }

    private fun cleanArticleBody(articleBody: Element) {
        articleBody.select(".entry-summary, #article-sidebar-wrapper, #sharingbaranchor, .ad, .authors, .teaserable-layout").remove()

        // remove scripts with try{window.performance.mark('monitor_articleTeaser');}catch(e){};
        articleBody.select("script").filter { it.html().contains("window.performance.mark") }.forEach { it.remove() }
    }

    private fun loadLazyLoadingElementsAndGetContent(siteContent: Element, articleBody: Element): String {
        extractInlineGalleries(articleBody)

        super.loadLazyLoadingElements(articleBody)

        var content = articleBody.html()

        siteContent.select(".topenrichment").first()?.let { topEnrichment ->
            topEnrichment.select("iframe").first()?.let { topEnrichmentIFrame ->
                loadLazyLoadingElement(topEnrichmentIFrame)
                content = topEnrichment.outerHtml() + content
            }
        }

        return content
    }

    private fun extractInlineGalleries(articleBody: Element) {
        articleBody.select("figure.gallery.inline").forEach { inlineGallery ->
            inlineGallery.select(".navigation").remove()

            inlineGallery.select("li").forEach { imageListElement ->
                imageListElement.remove()
                inlineGallery.append("<p>" + imageListElement.html() + "</p>")
            }
        }
    }


    private fun extractGalleryArticle(extractionResult: EntryExtractionResult, galleryArticleElement: Element, siteUrl: String) {
        val reference = extractReference(galleryArticleElement, siteUrl)

        val abstract = galleryArticleElement.select(".entry-summary").first()?.text() ?: ""

        galleryArticleElement.select("#article-body").first()?.let { articleBody ->
            val content = StringBuilder()
            readHtmlOfAllImagesInGallery(content, articleBody, siteUrl)

            articleBody.select(".offscreen").first()?.let { reference?.publishingDate = parseSueddeutscheDateString(it.text()) }

            extractionResult.setExtractedContent(Entry(content.toString(), abstract), reference)
        }
    }

    private fun readHtmlOfAllImagesInGallery(imageHtml: StringBuilder, articleBody: Element, siteUrl: String) {
        articleBody.select("img").first()?.let {
            imageHtml.append(loadLazyLoadingElement(it).outerHtml())
        }

        articleBody.select(".caption").first()?.let { caption ->
            caption.select("#article-sidebar-wrapper, .article-sidebar-wrapper, .date-copy").remove()
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

                return Reference(url, heading.text(), publishingDate, subTitle = subTitle)
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