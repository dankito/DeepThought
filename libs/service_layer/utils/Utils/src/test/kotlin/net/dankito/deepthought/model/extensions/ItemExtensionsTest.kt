package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.text.SimpleDateFormat

class ItemExtensionsTest {

    companion object {
        private const val Summary = "Summary"

        private const val SourceTitle = "Title"
        private const val SourceSubTitle = "SubTitle"

        private const val SourcePublishingDateString = "27.03.88"
        private val SourcePublishingDateFormat = SimpleDateFormat("dd.MM.yy") // in preview publishing date gets only displayed with short date format -> only to digits for year
        private val SourcePublishingDate = SourcePublishingDateFormat.parse(SourcePublishingDateString)

        private const val SeriesTitle = "Series"
        private val SourceSeries = Series(SeriesTitle)

        private const val Indication = "p. 117"
    }


    @Test
    fun sourcePreview_SourceWithoutSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle)

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceTitle)
    }

    @Test
    fun sourcePreview_SourceWithSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle, "", subTitle = SourceSubTitle)

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle)
    }


    @Test
    fun sourcePreview_SourceWithoutSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle)
        item.indication = Indication

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceTitle + " " + Indication)
    }

    @Test
    fun sourcePreview_SourceWithSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle, "", subTitle = SourceSubTitle)
        item.indication = Indication

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle + " " + Indication)
    }


    @Test
    fun sourcePreviewWithSeriesAndPublishingDate_SourceWithoutSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = createSource(false, true, true)

        // when
        val result = item.sourcePreviewWithSeriesAndPublishingDate

        // then
        assertThat(result).isEqualTo(SeriesTitle + " " + SourcePublishingDateString + " " + SourceTitle)
    }

    @Test
    fun sourcePreviewWithSeriesAndPublishingDate_SourceWithSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = createSource(true, true, true)

        // when
        val result = item.sourcePreviewWithSeriesAndPublishingDate

        // then
        assertThat(result).isEqualTo(SeriesTitle + " " + SourcePublishingDateString + " " + SourceSubTitle + ": " + SourceTitle)
    }


    @Test
    fun sourcePreviewWithSeriesAndPublishingDate_SourceWithoutSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = createSource(false, true, true)
        item.indication = Indication

        // when
        val result = item.sourcePreviewWithSeriesAndPublishingDate

        // then
        assertThat(result).isEqualTo(SeriesTitle + " " + SourcePublishingDateString + " " + SourceTitle + " " + Indication)
    }

    @Test
    fun sourcePreviewWithSeriesAndPublishingDate_SourceWithSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = createSource(true, true, true)
        item.indication = Indication

        // when
        val result = item.sourcePreviewWithSeriesAndPublishingDate

        // then
        assertThat(result).isEqualTo(SeriesTitle + " " + SourcePublishingDateString + " " + SourceSubTitle + ": " + SourceTitle + " " + Indication)
    }


    @Test
    fun sourcePreviewOrSummary_SourceWithoutSubTitle_NoIndication() {
        // given
        val item = Item("", Summary)
        item.source = createSource(false)

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(SourceTitle)
    }

    @Test
    fun sourcePreviewOrSummary_SourceWithSubTitle_NoIndication() {
        // given
        val item = Item("", Summary)
        item.source = createSource(true)

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle)
    }

    @Test
    fun sourcePreviewOrSummary_SourceWithoutSubTitle_IndicationSet() {
        // given
        val item = Item("", Summary)
        item.source = createSource(false)
        item.indication = Indication

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(SourceTitle + " " + Indication)
    }

    @Test
    fun sourcePreviewOrSummary_SourceWithSubTitle_IndicationSet() {
        // given
        val item = Item("", Summary)
        item.source = createSource(true)
        item.indication = Indication

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle + " " + Indication)
    }


    @Test
    fun sourcePreviewOrSummary_NoSource_NoIndication() {
        // given
        val item = Item("", Summary)

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(Summary)
    }

    @Test
    fun sourcePreviewOrSummary_NoSource_IndicationSet() {
        // given
        val item = Item("", Summary)
        item.indication = Indication

        // when
        val result = item.sourcePreviewOrSummary

        // then
        assertThat(result).isEqualTo(Indication)
    }


    private fun createSource(withSubTitle: Boolean = false, withSeries: Boolean = false, withPublishingDate: Boolean = false) =
            Source(SourceTitle, "",
                    if(withPublishingDate) SourcePublishingDate else null,
                    subTitle = if(withSubTitle) SourceSubTitle else "",
                    series = if(withSeries) SourceSeries else null)

}