package net.dankito.newsreader.feed

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.FeedArticleSummary
import net.dankito.webclient.extractor.AsyncResult
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.concurrent.thread


class RomeFeedReader : IFeedReader {

    companion object {
        private val log = LoggerFactory.getLogger(RomeFeedReader::class.java)
    }


    private val input = SyndFeedInput()


    override fun readFeedAsync(feedUrl: String, callback: (AsyncResult<FeedArticleSummary>) -> Unit) {
        thread {
            try {
                callback(AsyncResult(true, result = readFeed(feedUrl)))
            } catch(e: Exception) {
                log.error("Could not read feed from url " + feedUrl, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun readFeed(feedUrl: String) : FeedArticleSummary? {
        input.build(XmlReader(URL(feedUrl))).let { retrievedFeed ->
            val siteUrl = retrievedFeed.link ?: retrievedFeed.uri
            val summary = FeedArticleSummary(title = retrievedFeed.title, siteUrl = siteUrl, imageUrl = retrievedFeed.image?.url, publishedDate = retrievedFeed.publishedDate)

            mapEntries(summary, retrievedFeed)

            return summary
        }
    }

    private fun mapEntries(summary: FeedArticleSummary, retrievedFeed: SyndFeed) {
        summary.articles = retrievedFeed.entries.map { mapEntry(it) }
    }

    private fun mapEntry(syndEntry: SyndEntry): ArticleSummaryItem {
        val url = syndEntry.link ?: syndEntry.uri

        val item = ArticleSummaryItem(url, syndEntry.title, null, publishedDate = syndEntry.publishedDate, updatedDate = syndEntry.updatedDate)

        val abstractHtml: String? = tryToFindAbstractHtml(syndEntry)

        item.summary = extractSummary(syndEntry, abstractHtml)

        abstractHtml?.let {  Jsoup.parse(it)?.let {
            item.previewImageUrl = it.select("img").firstOrNull()?.attr("src")
        } }

        return item
    }

    private fun tryToFindAbstractHtml(syndEntry: SyndEntry): String? {
        var abstractHtml: String? = null

        for (content in syndEntry.contents) {
            if (content.type != null && content.type.contains("html")) {
                abstractHtml = content.value
                break
            }
        }

        if(abstractHtml == null) {
            if(syndEntry.description?.type?.contains("html") ?: false) {
                abstractHtml = syndEntry.description?.value
            }
        }

        return abstractHtml
    }

    private fun extractSummary(syndEntry: SyndEntry, abstractHtml: String?) : String {
        val summary = if (syndEntry.description != null && syndEntry.description.value.isNullOrBlank() == false) {
            syndEntry.description.value
        } else {
            abstractHtml ?: ""
        }

        return extractPlainTextFromHtml(summary)
    }

    private fun extractPlainTextFromHtml(html: String) : String {
        try {
            val doc = Jsoup.parseBodyFragment(html)
            // Trick for better formatting
            doc.body().wrap("<pre></pre>")

            var text = doc.text()
            // Converting nbsp entities
            text = text.replace("\u00A0".toRegex(), " ")

            return text
        } catch (ex: Exception) {
            log.error("Could not parse html " + html, ex)
        }

        return html
    }

}