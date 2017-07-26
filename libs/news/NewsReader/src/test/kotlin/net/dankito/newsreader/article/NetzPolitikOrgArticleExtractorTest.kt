package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test


class NetzPolitikOrgArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return NetzPolitikOrgArticleExtractor(webClient)
    }


    @Test
    fun extractNeueStrategieVonOReillyArticle() {
        getAndTestArticle("https://netzpolitik.org/2017/neue-strategie-von-oreilly-vom-open-source-vorkaempfer-zum-drm-profiteur/",
                "Neue Strategie von O’Reilly: Vom Open-Source-Vorkämpfer zum DRM-Profiteur",
                "<p>Der gemeinhin für seine Open-Source-Affinität bekannte O’Reilly-Verlag setzt in Zukunft primär auf ein Geschäftsmodell mit digitalem Kopierschutz (DRM) und Tracking. Nach Protesten soll es DRM-freie Bücher weiterhin geben, allerdings nur über Zwischenhändler.</p>",
                "https://cdn.netzpolitik.org/wp-upload/2017/07/OReilly-Books-860x484.png")
    }

    @Test
    fun extractTageszeitungWirftPolizeiBespitzelungVorArticle() {
        getAndTestArticle("https://netzpolitik.org/2017/tageszeitung-kieler-nachrichten-wirft-polizei-bespitzelung-vor/",
                "Tageszeitung „Kieler Nachrichten“ wirft Polizei Bespitzelung vor",
                "<p>Die „Kieler Nachrichten“ werfen der Landespolizei vor, zwei ihrer Journalisten überwacht zu haben. Am Auto des Chefredakteurs soll es einen Peilsender gegeben haben. Die Landesregierung weist die Anschuldigungen zurück. </p>",
                "https://cdn.netzpolitik.org/wp-upload/2017/07/KielerInnenFoerdeLuftaufnahme-860x484.jpg")
    }

}