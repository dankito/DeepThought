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
    fun importVideoArticle() {
        getAndTestArticle("http://www.zeit.de/politik/ausland/2017-10/spaniens-staatsanwaltschaft-erhebt-anklage-gegen-puigdemont",
                "Staatsanwaltschaft erhebt Anklage gegen Puigdemont",
                "Dem Regierungschef Kataloniens werden unter anderem Rebellion und Auflehnung gegen die Staatsgewalt vorgeworfen. Puigdemont soll nach Brüssel gereist sein.",
                null, 4000, false, "Spanien"
        )
    }

    @Test
    fun importPodcastArticle() {
        getAndTestArticle("http://www.zeit.de/wissen/gesundheit/2017-11/sexualitaet-weiblicher-orgasmus-hoehepunkt-probleme",
                "Der weibliche Orgasmus",
                "Jetzt stellen Hörer(innen) die Fragen: Unsere Sexpodcastfolgen zum Höhepunkt haben einiges ausgelöst. Deshalb gibt es noch mehr Wissen und Tipps zum Orgasmus der Frau.",
                null, 8500, false, "Podcast: Ist das normal / Sexualität"
        )
    }

    @Test
    fun ensureZeitPlusNotificationGetsShown() {
        getAndTestArticle("http://www.zeit.de/2017/45/afd-netzwerk-zeitschriften-stiftungen-verlage",
                "Ein aktives Netzwerk",
                "Ihr Ziel ist eine Revolution von rechts: Um die AfD scharen sich Dutzende Denkfabriken, Zeitschriften und Stiftungen. Wer sind ihre Vordenker?",
                null, 3400, false, "AfD"
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
