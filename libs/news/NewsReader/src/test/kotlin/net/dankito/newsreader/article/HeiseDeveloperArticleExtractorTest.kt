package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class HeiseDeveloperArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return HeiseDeveloperArticleExtractor(webClient)
    }


    @Test
    fun extractKotlinFuerAndroidArticle() {
        getAndTestArticle("https://www.heise.de/developer/meldung/Kommentar-Kotlin-fuer-Android-Googles-fremde-Lorbeeren-3717940.html",
                "Kommentar: Kotlin für Android – Googles fremde Lorbeeren",
                "Nach dem Wechsel von Eclipse zu IntellJ IDEA mag man die Kotlin-Unterstützung in Android als weiteren Ritterschlag verstehen. Doch der Geadelte ist hier nicht das Projekt Kotlin, sondern Google selbst – sagt Technologieexperte Benjamin Schmid.",
                "https://1.f.ix.de/scale/geometry/600/q75/imgs/18/2/2/0/4/5/3/5/kotlin-93ee585534495ad9.jpeg", 7500)
    }


    @Test
    fun extractMobileArticle() {
        getAndTestArticle("https://m.heise.de/developer/meldung/Java-EE-soll-unter-dem-Dach-der-Eclipse-Foundation-landen-3830121.html",
                "Java EE soll unter dem Dach der Eclipse Foundation landen",
                "Nach Gesprächen vor allem mit IBM und Red Hat veröffentlicht Oracle konkrete Pläne für die Zukunft von Java EE. So sollen unter anderem Referenzimplementierungen und Technology Compatibility Kits künftig an die Eclipse Foundation relizenziert werden.",
                "https://m.f.ix.de/scale/geometry/1280/q50/imgs/18/2/2/7/7/6/0/1/java-maschine-8e017762725b1260.jpeg@jpg", 4500)
    }

    @Test
    fun extractMultiPageMobileArticle() {
        getAndTestArticle("https://m.heise.de/developer/artikel/Java-am-Microcontroller-3813433.html",
                "Java am Microcontroller",
                null, null, 14500)
    }

}