package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.OkHttpWebClient
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.jsoup.Jsoup
import org.junit.Assert.assertThat
import org.junit.Test

class WebPageMetaDataExtractorTest {

    private val underTest = WebPageMetaDataExtractor(OkHttpWebClient())


    @Test
    fun extractMetaData() {
        val result = underTest.extractMetaData(Jsoup.parse("<html><head><meta name=\"date\" content=\"Mo, 30 Okt 2017 12:36:45 MEZ\" /></head></html>"))

        assertThat(result.publishingDateString, `is`("Mo, 30 Okt 2017 12:36:45 MEZ"))
        assertThat(result.publishingDate, notNullValue())
    }

}