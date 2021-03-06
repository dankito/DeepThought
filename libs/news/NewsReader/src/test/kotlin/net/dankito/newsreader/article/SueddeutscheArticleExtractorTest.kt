package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import org.junit.Test

class SueddeutscheArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return SueddeutscheArticleExtractor(webClient)
    }


    @Test
    fun extractSiegerImMarshmallowTestArticle() {
        getAndTestArticle("http://www.sueddeutsche.de/wissen/psychologie-sieger-im-marshmallow-test-1.3557488",
                "Sieger im \"Marshmallow-Test\"",
                "Kameruner Kinder zeigen mehr Beherrschung wenn es um eine versprochene Belohnung geht als Gleichaltrige aus Deutschland. Wie ist das zu interpretieren?",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3559806/940x528?v=1498468198000",
                5600)
    }

    @Test
    fun extractArticleWithInlineImageGallery() {
        getAndTestArticle("http://www.sueddeutsche.de/leben/kommune-revolution-am-bettrand-1.3544828",
                "Revolution am Bettrand",
                "Thomas Hesterberg machte 1967 das legendäre Foto der Kommune 1, auf dem die Bewohner ihre nackten Hintern der Kamera entgegenstrecken. Tatsächlich ging es in der Wohngemeinschaft gar nicht so freizügig zu.",
                null, 9000)
    }

    @Test
    fun extractArticleWithImageGallery() {
        getAndTestArticle("http://www.sueddeutsche.de/leben/kommune-revolution-am-bettrand-1.3548573",
                "Revolution am Bettrand",
                "Thomas Hesterberg machte 1967 das legendäre Foto der Kommune 1, auf dem die Bewohner ihre nackten Hintern der Kamera entgegenstrecken. Tatsächlich ging es dort gar nicht so freizügig zu. Wir zeigen weitere, bislang unveröffentlichte Bilder.",
                null, 16000, true)
    }

    // TODO:
//    @Test
//    fun extractArticleWithImage() {
//        getAndTestArticle("https://www.sueddeutsche.de/politik/krieg-im-jemen-die-welt-schaut-weg-1.4080943",
//                "Revolution am Bettrand",
//                "Thomas Hesterberg machte 1967 das legendäre Foto der Kommune 1, auf dem die Bewohner ihre nackten Hintern der Kamera entgegenstrecken. Tatsächlich ging es dort gar nicht so freizügig zu. Wir zeigen weitere, bislang unveröffentlichte Bilder.",
//                null, 16000, true)
//    }

    @Test
    fun extractArticleWithInlineImageCarousel() {
        getAndTestArticle("http://www.sueddeutsche.de/reise/uebernachten-im-wald-traeumen-unter-baeumen-1.3713211",
                "Träumen unter Bäumen",
                "Nachts im Wald, da kann einem schon mal mulmig werden. Aber nicht in diesen Unterkünften. Tipps zum Übernachten vom Ufo am Polarkreis bis zum Hobbit-Haus in Hessen.",
                null, 10500, subTitle = "Übernachten im Wald")
    }

    @Test
    fun extractArticleWithInlineIFrame() {
        getAndTestArticle("http://www.sueddeutsche.de/stil/test-nicht-nur-fuer-oma-1.3644869",
                "Nicht nur für Omas: Das ist das beste Trockenshampoo",
                "Sie sind besser für die Kopfhaut und sparen Zeit: Trockenshampoos feiern gerade ein Revival. Wir haben acht Produkte getestet.",
                null,
                2000) // first page has a length of little more than 2900
    }

    @Test
    fun extractArticleWithVideoInSummary() {
        getAndTestArticle("http://www.sueddeutsche.de/politik/spanien-madrid-uebernimmt-offiziell-kontrolle-in-katalonien-1.3728513",
                "Madrid ruft Puigdemont zur Teilnahme bei Neuwahl in Katalonien auf",
                null,
                null,
                2000, subTitle = "Katalonien")
    }

    @Test
    fun extractBarTip() {
        getAndTestArticle("https://www.sueddeutsche.de/muenchen/cafe-gartensalon-als-saesse-man-im-wohnzimmer-eines-astrid-lindgren-romans-1.3832224",
                "Als säße man im Wohnzimmer eines Astrid-Lindgren-Romans",
                "Das Café Gartensalon ist in vielerlei Hinsicht süß. Besonders verlockend ist die Kuchentheke.",
                "https://media-cdn.sueddeutsche.de/image/sz.1.3832423/640x360?v=1519293595000",
                4600, subTitle = "Café Gartensalon")
    }

    @Test
    fun ensureSurveysGetsRemoved() {
        getAndTestArticle("http://www.sueddeutsche.de/panorama/eil-bundesverfassungsgericht-fordert-drittes-geschlecht-im-geburtenregister-1.3740223",
                "Bundesverfassungsgericht fordert drittes Geschlecht im Geburtenregister",
                null,
                null,
                3200, subTitle = "Urteil zur Intersexualität")
    }

    @Test
    fun ensureSurveysGetsRemoved2() {
        getAndTestArticle("http://www.sueddeutsche.de/wirtschaft/agentur-fuer-arbeit-stellt-die-arbeitsagentur-hartz-iv-empfaenger-bloss-1.3747073",
                "Stellt die Arbeitsagentur Hartz-IV-Empfänger bloß?",
                null,
                null,
                4800, subTitle = "Agentur für Arbeit")
    }

    @Test
    fun ensureAuthorBiographyGetsRemoved() {
        getAndTestArticle("http://www.sueddeutsche.de/kultur/filmstarts-der-woche-welche-filme-sich-lohnen-und-welche-nicht-1.3751472",
                "Welche Filme sich lohnen und welche nicht",
                "In \"Animals\" steht Birgit Minichmayr im Mittelpunkt eines komisch-gruseligen Verwirrspiels und mit \"The Big Sick\" ist Michael Showalter eine wunderbar unverkrampfte Indie-Komödie gelungen.",
                null,
                5100, subTitle = "Filmstarts der Woche")
    }

    @Test
    fun ensureInterviewAmMorgenMessagesGetRemoved() {
        getAndTestArticle("http://www.sueddeutsche.de/wirtschaft/interview-am-morgen-man-redet-sich-mit-dem-wetter-heraus-1.3837657",
                "\"Man redet sich mit dem Wetter heraus\"",
                "Scharfschützen auf den Dächern, vermummte Polizisten in den Straßen: Abiturient und Juso-Vorstand Daniel Meier im \"Interview am Morgen\" über den Ausnahmezustand vor Trumps Besuch in Davos.",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3838573/640x360?v=1516783464000",
                4500, subTitle = "Interview am Morgen")
    }

    @Test
    fun ensureDataPollGetsRemoved() {
        getAndTestArticle("http://www.sueddeutsche.de/sport/sandro-wagner-ein-abschied-der-lauten-art-1.3983350",
                "Ein Abschied der lauten Art",
                "",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3983351/940x528?v=1526542261",
                4500, subTitle = "Sandro Wagner")
    }

    @Test
    fun ensureSZPlusNotificationGetsShown() {
        getAndTestArticle("http://www.sueddeutsche.de/wissen/menschen-ueber-wie-man-alt-und-zufrieden-wird-1.3733043?reduced=true",
                "Wie man alt und zufrieden wird",
                "Allein in Deutschland sind etwa 17 000 Menschen älter als 100 Jahre, und ihre Zahl steigt stetig. Studien zeigen, warum sie nicht nur ein langes, sondern oft auch zufriedenes Leben führen - und was Jüngere daraus lernen können.",
                null,
                1300, subTitle = "Menschen über 100")
    }

    @Test
    fun extractMultiPageArticle() {
        getAndTestArticle("http://www.sueddeutsche.de/medien/im-visier-der-nachbarn-al-jazeera-gefuerchtete-stimme-der-massen-1.3558089",
                "Al Jazeera - gefürchtete Stimme der Massen",
                "Mit seiner professionellen Machart erreicht der TV-Sender weltweit Millionen Zuschauer. Kritiker werfen ihm vor, den Terrorismus zu fördern. Saudi-Arabien will Katar nun zwingen, den Sender zu schließen.",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3558716/940x528?v=1498379214000",
                3000) // first page has a length of little more than 2900
    }

    @Test
    fun extractKarriereMultiPageArticle() {
        getAndTestArticle("https://www.sueddeutsche.de/karriere/sabbatical-job-auszeit-karriere-1.4212445",
                "Hilft eine Auszeit vom Job wirklich weiter?",
                "Batterien aufladen, das Leben entschleunigen, sich selbst finden: Die Erwartungen an ein Sabbatical sind hoch. An die Rückkehr denkt dabei fast niemand.",
                "https://media-cdn.sueddeutsche.de/image/sz.1.4212473/640x360?v=1542389482000",
                4400,
                subTitle = "Sabbatical") // first page has a length of little more than 4200
    }


}