package net.dankito.newsreader.article.recipes

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.ArticleExtractorBase
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document


class ChefkochArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Chefkoch"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return url.contains("://www.chefkoch.de/rezepte/") && url.length > "http://www.chefkoch.de/rezepte/".length + 4
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("div.content-wrapper.clearfix").first()?.let { contentWrapper ->
            var title = ""
            var content = ""

            contentWrapper.select("h1.page-title").first()?.let { pageTitle ->
                title = pageTitle.text().trim()
                content += pageTitle.outerHtml()
            }

            contentWrapper.select("div.summary").first()?.let { summaryElement ->
                content += summaryElement.outerHtml()
            }

            // TODO: extract images
//            content += contentWrapper.select("#slideshow").first()?.outerHtml() ?: ""

            contentWrapper.select("div.main-content").first()?.let { mainContent ->
                mainContent.select(".flex--desktop, #text-ads-rezeptbild, .how2video-container, .mobile-only, .order-online, .recipe2shoppinglist," +
                        "#text-ads-unter-zubereitung, #recipe-com-user-footer, .sfs-esi-positioning-container, #rezeptvideos").remove()

                // TODO: may also get videos to work, remove #rezeptvideos then from above's content cleaning

                // TODO: get ingredient form to work
                mainContent.select("#incredientform").remove()

                content += mainContent.outerHtml()
            }

            extractionResult.setExtractedContent(Item(content), Source(title, url))
        }
    }

}