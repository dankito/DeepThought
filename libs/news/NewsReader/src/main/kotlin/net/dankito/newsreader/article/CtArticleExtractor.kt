package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.authentication.HeiseAuthenticator
import net.dankito.newsreader.model.LoginResult
import net.dankito.utils.credentials.ICredentials
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class CtArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val heiseDateTimeFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)

        private val heiseDateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

        private val ctMobileDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)

        private val AccessDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)

        private val log = LoggerFactory.getLogger(CtArticleExtractor::class.java)
    }


    private val authenticator = HeiseAuthenticator(webClient)

    private var triedToResolveMultiPageArticle = false


    override fun getName(): String? {
        return "c't"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.heise.de/ct/")
                || isHttpOrHttpsUrlFromHost(url, "m.heise.de/ct/")
                || isHttpOrHttpsUrlFromHost(url, ".heise.de/select/ct")
    }


    override val doesSupportLoggingIn = true


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        if(tryToParseMultiPageArticle(extractionResult, document, url)) {
            return
        }

        document.body().select("article.article--variante-2").firstOrNull()?.let { articleElement ->
            parseArticleSite(url, articleElement, extractionResult)
            handleNeedsLoginToViewFullArticle(url, document, extractionResult)
            return
        }

        document.body().select("main section:not(.meta)").firstOrNull()?.let { sectionElement ->
            parseDesktopSite(url, sectionElement, extractionResult)
            handleNeedsLoginToViewFullArticle(url, document, extractionResult)
            return
        }

        document.body().select("article").first()?.let { articleElement ->
            parseMobileSite(url, articleElement, extractionResult)
        }

        handleNeedsLoginToViewFullArticle(url, document, extractionResult)
    }


    private fun parseArticleSite(url: String, articleElement: Element, extractionResult: ItemExtractionResult) {
        articleElement.select("header").firstOrNull()?.let { headerElement ->
            val title = headerElement.select("h1").firstOrNull()?.text()?.trim() ?: ""
            val subtitle = headerElement.select("h2").firstOrNull()?.text()?.trim() ?: ""

            val previewImage = headerElement.select("figure").firstOrNull()
            val previewImageUrl = previewImage?.select("img")?.firstOrNull()?.attr("src")
            val publishingDate = extractArticlePublishingDate(articleElement.ownerDocument())

            val item = Item(articleElement.outerHtml())
            item.indication = articleElement.ownerDocument().select(".meta__item--page").firstOrNull()?.text()?.trim() ?: ""

            val source = Source(title, url, publishingDate, previewImageUrl, subtitle)
            source.issue = articleElement.ownerDocument().select(".meta__item--issue").firstOrNull()?.text()?.trim()?.replace("c't ", "")

            extractionResult.setExtractedContent(item, source)
        }
    }

    private fun extractArticlePublishingDate(document: Document): Date? {
        try {
            val body = document.body().html()

            var startIndex = body.indexOf("subscriberAccessDate: ")
            if (startIndex > 0) {
                startIndex += "subscriberAccessDate: ".length
                val endIndex = body.indexOf("\n", startIndex)

                if (endIndex > 0 && (endIndex - startIndex) < 30) {
                    val accessDateString = body.substring(startIndex + 1, endIndex - 1)

                    return AccessDateFormat.parse(accessDateString)
                }
            }
        } catch (e: Exception) {
            log.error("Could not parse subscriberAccessDate", e)
        }

        return null
    }


    private fun parseDesktopSite(url: String, sectionElement: Element, extractionResult: ItemExtractionResult) {
        val articleItem = createItem(url, sectionElement)

        val source = createSource(url, sectionElement)

        extractionResult.setExtractedContent(articleItem, source)
    }

    private fun createItem(url: String, sectionElement: Element): Item {
        var content = ""

        makeLinksAbsolute(sectionElement, url)
        adjustSourceElements(sectionElement)

        sectionElement.select("div.article_page_img img").first()?.let { pageImgElement ->
            content += pageImgElement.outerHtml()
        }

        sectionElement.select("p.article_page_intro strong").first()?.let { summaryElement ->
            content += summaryElement.parent().outerHtml()
        }

        sectionElement.select("div.article_page_text").first()?.let { articlePageTextElement ->
            var pageTextElement = articlePageTextElement
            articlePageTextElement.select("div.article_text").first()?.let { pageTextElement = it }

            pageTextElement.children()
                    .filter { shouldFilterParagraph(it) == false }
                    .forEach { content += it.outerHtml() }
        }

        content += extractPatchesIfAny(sectionElement)

        return Item(content)
    }

    private fun shouldFilterParagraph(paragraph: Element): Boolean {
        return paragraph.text().isBlank() || paragraph.hasClass("akwa-ad-container")
    }

    private fun extractPatchesIfAny(sectionElement: Element): String {
        var patchesHtml = ""
        val sectionElementParent = sectionElement.parent()

        sectionElementParent.select("p.article_page_patch_title").first()?.let { patchTitleElement ->
            patchesHtml += "<hr/>"
            patchesHtml += patchTitleElement.outerHtml()

            val nextSibling = patchTitleElement.nextElementSibling()
            if(nextSibling != null && "p" == nextSibling.nodeName() && nextSibling.classNames().size == 1 && "" == nextSibling.className()) {
                patchesHtml += nextSibling.outerHtml()
            }
        }

        sectionElementParent.select("p.article_page_patch_date").first()?.let { patchDateElement ->
            patchesHtml += patchDateElement.outerHtml()
        }

        sectionElementParent.select("div.article_page_patch_text").first()?.let { patchTextElement ->
            patchesHtml += patchTextElement.outerHtml()
        }

        return patchesHtml
    }


    private fun createSource(articleUrl: String, sectionElement: Element): Source {
        val headerElement = sectionElement.select("header").first()

        val title = headerElement.select("h1").text()

        var subTitle = ""
        headerElement.select("h2").first()?.let { subTitleElement ->
            subTitle = subTitleElement.text()
        }

        val publishingDate = extractDate(sectionElement)

        return Source(title, articleUrl, publishingDate, subTitle = subTitle)
    }


    private fun parseMobileSite(url: String, article: Element, extractionResult: ItemExtractionResult) {
        val source = extractMobileArticleSource(article, url)

        article.select("h1, time, span.author, a.comments, .comment, .btn-toolbar .whatsbroadcast-toolbar, #whatsbroadcast, " +
                ".btn-group, .whatsbroadcast-group, .shariff, .ISI_IGNORE, .article_meta, .widget-werbung").remove()
        val content = article.html()

        extractionResult.setExtractedContent(Item(content), source)
    }

    private fun extractMobileArticleSource(article: Element, url: String): Source {
        val title = article.select("h1").first()?.text()?.trim() ?: ""

        val source = Source(title, url)

        article.select("figure.aufmacherbild img").first()?.let {
            source.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
        }
        article.select("time").first()?.let {
            try {
                source.publishingDate = ctMobileDateFormat.parse(it.attr("datetime"))
            } catch(e: Exception) { log.warn("Could not parse C't mobile site date string ${it.attr("datetime")}", e) }
        }

        return source
    }


    private fun extractDate(sectionElement: Element): Date? {
        sectionElement.select("time").first()?.let { articleDateTimeElement ->
            parseHeiseDateTimeFormat(articleDateTimeElement.text())?.let { return it }

            if(articleDateTimeElement.hasAttr("datetime")) {
                return parseIsoDateTimeStringWithoutTimezone(articleDateTimeElement.attr("datetime"))
            }
        }

        return null
    }

    private fun parseHeiseDateTimeFormat(articleDateTime: String): Date? {
        var editableArticleDateTime = articleDateTime
        editableArticleDateTime = editableArticleDateTime.replace("&nbsp;", "")
        editableArticleDateTime = editableArticleDateTime.replace(" Uhr", "").trim { it <= ' ' }
        editableArticleDateTime = editableArticleDateTime.replace("zuletzt geändert ", "")

        try {
            return heiseDateTimeFormat.parse(editableArticleDateTime)
        } catch (e: Exception) {
            log.error("Could not parse Heise DateTime Format " + editableArticleDateTime, e)
        }

        try {
            return heiseDateFormat.parse(editableArticleDateTime)
        } catch (e: Exception) {
            log.error("Could not parse Heise Date Format " + editableArticleDateTime, e)
        }

        return null
    }


    private fun tryToParseMultiPageArticle(extractionResult: ItemExtractionResult, document: Document, url: String): Boolean {
        if(triedToResolveMultiPageArticle == false) {
            getAllOnOnePageLink(document, url)?.let { allOnOnePageUrl ->
                triedToResolveMultiPageArticle = true

                extractArticle(allOnOnePageUrl)?.let { result ->
                    if(result.couldExtractContent) {
                        extractionResult.setExtractedContent(result.item, result.source)
                        return true
                    }
                }
            }
        }

        triedToResolveMultiPageArticle = false

        return false
    }

    private fun getAllOnOnePageLink(document: Document, articleUrl: String): String? {
        document.body().select(".article-pages-summary__onepage").first()?.let { allOnOnePageAnchor ->
            return makeLinkAbsolute(allOnOnePageAnchor.attr("href"), articleUrl)
        }

        document.body().select(".seite_weiter, .article_pagination .next").first()?.let { nextPageAnchor ->
            return tryToGetAllOnOnePageLinkManually(nextPageAnchor, articleUrl)
        }

        return null
    }

    private fun tryToGetAllOnOnePageLinkManually(nextPageAnchor: Element, articleUrl: String): String? {
        val nextPageUrl = makeLinkAbsolute(nextPageAnchor.attr("href"), articleUrl)
        val index = nextPageUrl.indexOf("seite=")

        if(index > 0) {
            val pageNumberStartIndex = index + "seite=".length
            val pageNumberEndIndex = getPageNumberEndIndex(pageNumberStartIndex, nextPageUrl)

            try {
                return nextPageUrl.replaceRange(pageNumberStartIndex, pageNumberEndIndex, "all")
            } catch(e: Exception) { log.error("Could not get all on one page url from $nextPageUrl", e) }
        }

        return null
    }

    private fun getPageNumberEndIndex(pageNumberStartIndex: Int, nextPageUrl: String): Int {
        var pageNumberEndIndex = pageNumberStartIndex + 1
        val indexOfNextAmpersand = nextPageUrl.indexOf('&', pageNumberStartIndex + 1)

        if(nextPageUrl.length - pageNumberStartIndex <= 2) {
            pageNumberEndIndex = nextPageUrl.length
        }
        else if(indexOfNextAmpersand > 0) {
            pageNumberEndIndex = indexOfNextAmpersand - 1
        }

        return pageNumberEndIndex
    }


    override fun needsLoginToViewFullArticle(url: String, document: Document): Boolean {
        return document.body().select("#purchase").isNotEmpty()
    }

    override fun login(credentials: ICredentials): LoginResult? {
        authenticator.login(credentials)?.let { loginCookie ->
            return setLoginResult(listOf(loginCookie))
        }

        return super.login(credentials)
    }

}
