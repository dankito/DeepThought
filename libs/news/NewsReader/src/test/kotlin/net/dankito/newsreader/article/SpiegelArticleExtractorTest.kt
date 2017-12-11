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
                "Armer, reicher Hedgefonds-König", null,
                "http://cdn2.spiegel.de/images/image-83501-860_panofree-rmyr-83501.jpg", 8600, subTitle = "Wall-Street-Mogul Steve Cohen")
    }

    @Test
    fun importArticleWithUnorderedList() {
        getAndTestArticle("http://www.spiegel.de/wirtschaft/soziales/griechenland-so-gefaehrlich-waere-der-grexit-a-1038609.html",
                "So gefährlich wäre Griechenlands Euro-Aus", null,
                "http://cdn3.spiegel.de/images/image-860556-breitwandaufmacher-qbik-860556.jpg", 7800, subTitle = "Drohender Staatsbankrott"
                )
    }

    //  @Test
    //  public void importArticle() {
    //    Item importedEntry = testImportArticle("http://www.spiegel.de/panorama/polizisten-funken-goebbels-zitat-vor-g7-gipfel-a-1038563.html");
    //    testImportedArticleValues(importedEntry, null, 8626, "14.06.2010", "Armer, reicher Hedgefonds-König", "Wall-Street-Mogul Steve Cohen",
    //        "Er lebt weiter in Saus und Braus, als hätte es die Wirtschaftskrise nie gegeben: Steve Cohen ist einer der legendärsten Hedgefondsmanager der Wall Street - und der wohl meistgehasste. Jetzt offenbart der Milliardär in einem Interview erstmals sein Privatleben.");
    //  }
}
