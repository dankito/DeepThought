package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test


class SpiegelArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return SpiegelArticleExtractor(webClient)
    }


    @Test
    fun importWallStreetMogulSteveCohenArmerReicherHedgefondsKönigArticle() {
        getAndTestArticle("http://www.spiegel.de/wirtschaft/unternehmen/wall-street-mogul-steve-cohen-armer-reicher-hedgefonds-koenig-a-700480.html",
                "Armer, reicher Hedgefonds-König",
                "Er lebt weiter in Saus und Braus, als hätte es die Wirtschaftskrise nie gegeben: Steve Cohen ist einer der legendärsten Hedgefondsmanager der Wall Street - und" +
                        " der wohl meistgehasste. Jetzt offenbart der Milliardär in einem Interview erstmals sein Privatleben.",
                null, 8600)
    }

    @Test
    fun importArticleWithUnorderedList() {
        getAndTestArticle("http://www.spiegel.de/wirtschaft/soziales/griechenland-so-gefaehrlich-waere-der-grexit-a-1038609.html",
                "So gefährlich wäre Griechenlands Euro-Aus",
                "Der Grexit? Ein Kinderspiel. In Deutschland werden die Folgen eines griechischen Staatsbankrotts und Euro-Austritts kleingeredet. Doch die nervösen Reaktionen an den Börsen zeigen: Das Szenario wäre alles andere als harmlos.",
                null, 7800
                )
    }

    //  @Test
    //  public void importArticle() {
    //    Entry importedEntry = testImportArticle("http://www.spiegel.de/panorama/polizisten-funken-goebbels-zitat-vor-g7-gipfel-a-1038563.html");
    //    testImportedArticleValues(importedEntry, null, 8626, "14.06.2010", "Armer, reicher Hedgefonds-König", "Wall-Street-Mogul Steve Cohen",
    //        "Er lebt weiter in Saus und Braus, als hätte es die Wirtschaftskrise nie gegeben: Steve Cohen ist einer der legendärsten Hedgefondsmanager der Wall Street - und der wohl meistgehasste. Jetzt offenbart der Milliardär in einem Interview erstmals sein Privatleben.");
    //  }
}
