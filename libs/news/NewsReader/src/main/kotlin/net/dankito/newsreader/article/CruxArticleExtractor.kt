package net.dankito.newsreader.article

import com.chimbori.crux.articles.ArticleExtractor
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document


class CruxArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return null
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return true
    }

    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        val article = ArticleExtractor(url, document)
                .extractMetadata()
                .extractContent()
                .article()

        extractionResult.setExtractedContent(Entry(article.document.outerHtml(), article.description),
                Reference(url, article.title, previewImageUrl = makeLinkAbsolute(article.imageUrl, url)))
    }
}