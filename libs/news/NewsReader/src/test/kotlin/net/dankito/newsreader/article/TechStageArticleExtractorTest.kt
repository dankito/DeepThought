package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import org.junit.Test

class TechStageArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return TechStageArticleExtractor(webClient)
    }


    @Test
    fun extractMotorolaMotoCTestArticle() {
        getAndTestArticle("http://www.techstage.de/test/Lenovo-Motorola-Moto-C-100-Euro-Smartphone-im-Test-3813253.html",
                "Lenovo Motorola Moto C: 100-Euro-Smartphone im Test",
                "Von Lenovo/Motorola gibt es recht günstige Smartphones, die uns im Test teils sehr gut gefallen haben. Mit dem Moto C bringt das Unternehmen nun ein noch billigeres Handy auf den Markt, es kostet nur knapp 100 Euro. Da darf man durchaus skeptisch sein, dass dieses Gerät was taugt. Ob das Moto C unsere Bedenken zerstreuen konnte, erfahrt Ihr in unserem Test.",
                "http://www.techstage.de/imgs/85/2/2/6/6/3/5/7/Moto_C_16_9-ed6219fd22ffd7da.jpeg", 6200)
    }

}