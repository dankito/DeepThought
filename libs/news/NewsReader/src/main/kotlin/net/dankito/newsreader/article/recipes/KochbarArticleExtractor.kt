package net.dankito.newsreader.article.recipes

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.ArticleExtractorBase
import org.jsoup.nodes.Document

class KochbarArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Kochbar"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("https://www.kochbar.de/rezept/") && url.length > "https://www.kochbar.de/rezept/".length
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select(".rezepte").first()?.let { recipeElement ->
            var title = ""
            var content = ""
            var summary = ""

            recipeElement.select(".kb-recipe-headline h1").first()?.let { titleElement ->
                title = titleElement.text().trim()
                content += titleElement.outerHtml()
            }

            recipeElement.select(".subheadline").first()?.let { subHeadLine ->
                summary = subHeadLine.text().trim()
                content += subHeadLine.outerHtml()
            }

            val source = Source(title, url)

            recipeElement.select(".kb-recipe-headline > img").first()?.let { previewImage ->
                source.previewImageUrl = previewImage.attr("src")
                content += "<p>" + previewImage.outerHtml() + "</p>"
            }

            recipeElement.select(".kb-badge-container-title").first()?.let {
                content += "<h2>" + it.outerHtml() + "</h2>"
            }
            content += recipeElement.select(".kb-recipe-ingredient-table-wrapper").first()?.outerHtml() ?: ""

            recipeElement.select(".kb-recipe-badge-wrapper").first()?.let { stepsTitle ->
                stepsTitle.select(".kb-h2-text").remove()
                content += stepsTitle.outerHtml()
            }
            recipeElement.select(".kb-steps-wrapper").first()?.let { steps ->
                steps.select(".kb-read-more, .kb-spacer-10").remove()
                content += steps.outerHtml()
            }


            extractionResult.setExtractedContent(Item(content, summary), source)
        }
    }

}