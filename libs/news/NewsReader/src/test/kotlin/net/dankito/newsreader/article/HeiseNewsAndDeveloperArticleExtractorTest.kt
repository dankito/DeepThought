package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class HeiseNewsAndDeveloperArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return HeiseNewsAndDeveloperArticleExtractor(webClient)
    }


    @Test
    fun extractKotlinFuerAndroidArticle() {
        getAndTestArticle("https://www.heise.de/developer/meldung/Kommentar-Kotlin-fuer-Android-Googles-fremde-Lorbeeren-3717940.html",
                "Kommentar: Kotlin für Android – Googles fremde Lorbeeren",
                "Nach dem Wechsel von Eclipse zu IntellJ IDEA mag man die Kotlin-Unterstützung in Android als weiteren Ritterschlag verstehen. Doch der Geadelte ist hier nicht das Projekt Kotlin, sondern Google selbst – sagt Technologieexperte Benjamin Schmid.")
    }

}