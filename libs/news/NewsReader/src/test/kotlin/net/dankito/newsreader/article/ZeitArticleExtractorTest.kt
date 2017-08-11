package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test


class ZeitArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return ZeitArticleExtractor(webClient)
    }


    @Test
    fun importSinglePageArticle() {
        getAndTestArticle("http://www.zeit.de/politik/ausland/2015-06/haftbefehl-aegypten-berlin-tegel-journalist-festnahme",
                "Ägyptischer Journalist an Berliner Flughafen festgenommen",
                "Die Polizei hat am Flughafen Tegel einen Al-Dschasira-Journalisten festgesetzt, die ägyptische Regierung wirft ihm Folter vor. Der Sender fühlt sich politisch vorfolgt.",
                null, 3400, false, "Haftbefehl"
        )
    }

    @Test
    fun importMultiPageArticle() {
        getAndTestArticle("http://www.zeit.de/digital/internet/2017-08/dna-malware-hacker",
                "Mit Spucke einen Computer hacken",
                "Dieser Hack ist haarsträubend komplex und völlig realitätsfremd – aber auch großartig: Forscher haben eine Schadsoftware in DNA versteckt und so einen Computer gekapert.",
                "http://img.zeit.de/digital/internet/2017-08/dna-malware-hacker-bild/wide__822x462", 5800, false, "DNA"
        )
    }

}
